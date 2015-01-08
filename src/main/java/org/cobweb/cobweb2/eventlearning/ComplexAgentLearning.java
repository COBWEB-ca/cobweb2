package org.cobweb.cobweb2.eventlearning;

import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.Direction;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.Rotation;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;

//Food storage
//Vaccination/avoid infected agents
//Wordbuilding

//Make learning toggleable
//React to temperatures


public class ComplexAgentLearning extends ComplexAgent {

	public ComplexAgentLearning(SimulationInternals sim) {
		super(sim);
	}

	private static final long serialVersionUID = 6166561879146733801L;

	@Deprecated //FIXME static!
	public static List<Occurrence> allOccurrences = new LinkedList<Occurrence>();

	/**
	 * A collection of events in memory.
	 */
	public List<MemorableEvent> memEvents;


	private List<Queueable> queueables;

	/**
	 * MemorableEvents are placed in the agent's memory with this method. Earliest memories will
	 * be forgotten when the memory limit is exceeded.
	 * <br />
	 * TODO: Forget memories as time passes
	 */
	public void remember(MemorableEvent event) {
		if (event == null) {
			return;
		}
		if (memEvents == null) {
			memEvents = new LinkedList<MemorableEvent>();
		}
		memEvents.add(event);
		if (memEvents.size() > lParams.numMemories) {
			memEvents.remove(0);
		}
	}

	/**
	 * Events are queued using this method
	 * @param act The action to add to the queue.
	 */
	public void queue(Queueable act) {
		if (act == null) {
			return;
		}

		if (queueables == null) {
			queueables = new LinkedList<Queueable>();
		}

		queueables.add(act);
	}


	private LocationDirection breedPos = null;


	public LearningAgentParams lParams;

	public long getCurrTick() {
		return currTick;
	}

	/**
	 * Add the given amount to the agent's energy.
	 * @param amount Amount to add.
	 */
	public void changeEnergy(int amount) {
		energy += amount;
	}

	@Override
	protected void broadcastFood(Location loc) { // []SK
		super.broadcastFood(loc);

		// Deduct broadcasting cost from energy
		if (energy > 0) {
			// If still alive, the agent remembers that it is displeasing to
			// lose energy
			// due to broadcasting
			float howSadThisMakesMe = Math.max(Math.min((float) -params.broadcastEnergyCost / (float) energy, 1), -1);
			remember(new MemorableEvent(currTick, howSadThisMakesMe, "broadcast"));
		}
	}

	@Override
	public void eat(Location destPos) {
		if (environment.getFoodType(destPos) == params.type) {
			// Eating food is ideal!!
			remember(new MemorableEvent(currTick, lParams.foodPleasure, "food"));
		} else {
			// Eating other food has a ratio of goodness compared to eating
			// normal food.
			float howHappyThisMakesMe = (float) params.otherFoodEnergy / (float) params.foodEnergy
					* lParams.foodPleasure;
			remember(new MemorableEvent(currTick, howHappyThisMakesMe, "food"));
		}

		super.eat(destPos);
	}

	@Override
	protected void eat(ComplexAgent adjacentAgent) {
		super.eat(adjacentAgent);
		// Bloodily consuming agents makes us happy
		remember(new MemorableEvent(currTick, lParams.ateAgentPleasure, "ateAgent"));
	}

	@Override
	protected void iveBeenCheated(long othersID) {
		super.iveBeenCheated(othersID);
		remember(new MemorableEvent(currTick, -1, "agent-" + othersID));
	}

	@Override
	protected void receiveBroadcast() {
		super.receiveBroadcast();
		// TODO: Add a MemorableEvent to show a degree of friendliness towards
		// the broadcaster
	}


	@Override
	public void step() {
		Agent adjAgent;
		final LocationDirection destPos = environment.topology.getAdjacent(getPosition());

		if (canStep(destPos)) {

			// Check for food...
			if (environment.hasFood(destPos)) {

				// Queues the agent to broadcast about the food
				queue(new SmartAction(this, "broadcast") {

					@Override
					public void desiredAction() {
						if (params.broadcastMode & canBroadcast()) {
							agent.broadcastFood(destPos);

							// Remember a sense of pleasure from helping out
							// other agents by broadcasting
							agent.remember(new MemorableEvent(currTick, lParams.broadcastPleasure, "broadcast"));
						}
					}

				});

				if (canEat(destPos)) {
					// Queue action to eat the food
					queue(new SmartAction(this, "food") {

						@Override
						public void desiredAction() {
							agent.eat(destPos);
						}

					});
				}

				if (pregnant && energy >= params.breedEnergy && pregPeriod <= 0) {

					queue(new BreedInitiationOccurrence(this, 0, "breedInit", breedPartner));

				} else {
					if (!pregnant) {
						// Manages asexual breeding
						queue(new SmartAction(this, "asexBreed") {

							@Override
							public void desiredAction() {
								agent.tryAsexBreed();
							}

						});
					}
				}
			}

			queue(new Occurrence(this, 0, "stepMutate") {
				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					getAgentListener().onStep(concernedAgent, getPosition(), destPos);
					return null;
				}
			});

			// Move the agent to destPos
			queue(new SmartAction(this, "move-" + destPos.toString()) {

				@Override
				public void desiredAction() {
					agent.move(destPos);
				}
			});

			// Try to breed
			queue(new Occurrence(this, 0, "breed") {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					if (concernedAgent.getBreedPos() != null) {

						if (concernedAgent.breedPartner == null) {
							concernedAgent.getInfo().addDirectChild();
							ComplexAgentLearning child = concernedAgent.createChildAsexual(concernedAgent.getBreedPos());

							// Retain emotions for our child!
							concernedAgent.remember(new MemorableEvent(currTick, lParams.emotionForChildren, "agent-" + child.stats.id));
						} else {
							// child's strategy is determined by its parents, it
							// has a
							// 50% chance to get either parent's strategy

							// We like the agent we are breeding with; remember
							// that
							// this agent is favourable
							concernedAgent.remember(new MemorableEvent(currTick, lParams.loveForPartner, "agent-" + breedPartner.stats.id));

							concernedAgent.getInfo().addDirectChild();
							concernedAgent.breedPartner.getInfo().addDirectChild();
							ComplexAgentLearning child = concernedAgent.createChildSexual(
									concernedAgent.getBreedPos(),
									(ComplexAgentLearning)concernedAgent.breedPartner);

							// Retain an undying feeling of love for our
							// child
							MemorableEvent weLoveOurChild = new MemorableEvent(currTick, lParams.emotionForChildren, "" + child);
							concernedAgent.remember(weLoveOurChild);
							((ComplexAgentLearning)concernedAgent.breedPartner).remember(weLoveOurChild);

							concernedAgent.getInfo().addSexPreg();
						}
						concernedAgent.breedPartner = null;
						concernedAgent.pregnant = false; // Is this boolean even
						// necessary?
						setBreedPos(null);
					}
					return null;
				}
			});

			// Lose energy from stepping
			queue(new EnergyChangeOccurrence(this, -params.stepEnergy, "step") {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					MemorableEvent ret = super.effect(concernedAgent);

					concernedAgent.setWasteCounterLoss(getWasteCounterLoss() - concernedAgent.params.stepEnergy);
					concernedAgent.getInfo().useStepEnergy(params.stepEnergy);
					concernedAgent.getInfo().addPathStep(concernedAgent.getPosition());

					return ret;
				}
			});

		} else if ((adjAgent = getAdjacentAgent()) != null && adjAgent instanceof ComplexAgentLearning
				&& ((ComplexAgentLearning) adjAgent).stats != null) {
			// two agents meet

			final ComplexAgentLearning adjacentAgent = (ComplexAgentLearning) adjAgent;

			queue(new Occurrence(this, 0, "contactMutate") {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					getAgentListener().onContact(concernedAgent, adjacentAgent);
					return null;
				}
			});

			if (canEat(adjacentAgent)) {
				//An action to conditionally eat the agent
				queue(new SmartAction(this, "agent-" + adjacentAgent.stats.id) {

					@Override
					public void desiredAction() {
						agent.eat(adjacentAgent);
					}

					@Override
					public boolean actionIsDesireable() {
						// 0.3f means we have a positive attitude towards this
						// agent. If an agent needs has an "appreciation value"
						// of 0.3 or less it will be eaten
						return totalMagnitude() < lParams.eatAgentEmotionalThreshold;
					}

					@Override
					public void actionIfUndesireable() {
						// Agent in question needs to appreciate the fact that
						// we didn't just EAT HIM ALIVE.
						adjacentAgent.remember(new MemorableEvent(currTick, lParams.sparedEmotion, "agent-" + stats.id));
					}
				});
			}

			// if the agents are of the same type, check if they have enough
			// resources to breed
			if (adjacentAgent.params.type == params.type) {

				double sim = 0.0;
				boolean canBreed = !pregnant && energy >= params.breedEnergy && params.sexualBreedChance != 0.0
						&& getRandom().nextFloat() < params.sexualBreedChance;

				// Generate genetic similarity number
				sim = calculateSimilarity(adjacentAgent);

				if (sim >= params.commSimMin) {
					// Communicate with the smiliar agent
					queue(new SmartAction(this, "communicate") {

						@Override
						public void desiredAction() {
							agent.communicate(adjacentAgent);
						}
					});
				}

				if (canBreed && sim >= params.breedSimMin
						&& checkCredibility(adjacentAgent.stats.id) && adjacentAgent.checkCredibility(this.stats.id)) {
					// Initiate pregnancy
					queue(new SmartAction(this, "breed") {

						@Override
						public void desiredAction() {
							agent.pregnant = true;
							agent.pregPeriod = agent.params.sexualPregnancyPeriod;
							agent.breedPartner = adjacentAgent;
						}
					});

				}
			}

			if (!pregnant && checkCredibility(adjacentAgent.stats.id) && adjacentAgent.checkCredibility(this.stats.id)) {

				queue(new SmartAction(this) {

					@Override
					public void desiredAction() {
						agent.playPDonStep(adjacentAgent, adjacentAgent.stats.id);
					}
				});

			}
			energy -= params.stepAgentEnergy;
			setWasteCounterLoss(getWasteCounterLoss() - params.stepAgentEnergy);
			stats.useAgentBumpEnergy(params.stepAgentEnergy);

		} // end of two agents meet
		else if (destPos != null && environment.hasDrop(destPos)) {

			// Allow agents up to a distance of 5 to see this agent hit the
			// waste
			queue(new Occurrence(this, 5, "bumpWaste") {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					concernedAgent.queue(new EnergyChangeOccurrence(concernedAgent, -params.wastePen, "bumpWaste"));
					setWasteCounterLoss(getWasteCounterLoss() - params.wastePen);
					stats.useRockBumpEnergy(params.wastePen);
					return null;
				}
			});

		} else {
			// Rock bump
			queue(new Occurrence(this, 0, "bumpRock") {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					concernedAgent
					.queue(new EnergyChangeOccurrence(concernedAgent, -params.stepRockEnergy, "bumpRock"));
					setWasteCounterLoss(getWasteCounterLoss() - params.stepRockEnergy);
					stats.useRockBumpEnergy(params.stepRockEnergy);
					return null;
				}
			});
		}

		// Energy penalty
		queue(new EnergyChangeOccurrence(this, -(int) energyPenalty(), "energyPenalty"));

		if (energy <= 0)
			queue(new SmartAction(this) {

				@Override
				public void desiredAction() {
					agent.die();
				}
			});

		if (energy < params.breedEnergy) {
			queue(new SmartAction(this) {

				@Override
				public void desiredAction() {
					agent.pregnant = false;
					agent.breedPartner = null;
				}
			});
		}

		if (pregnant) {
			// Reduce pregnancy period
			queue(new Occurrence(this, 0, "preg") {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					concernedAgent.pregPeriod--;
					return null;
				}
			});
		}
	}

	@Override
	protected ComplexAgentLearning createChildAsexual(LocationDirection location) {
		ComplexAgentLearning child = new ComplexAgentLearning(simulation);
		child.init(environment, location, this);
		return child;
	}

	private ComplexAgentLearning createChildSexual(LocationDirection location, ComplexAgentLearning otherParent) {
		ComplexAgentLearning child = new ComplexAgentLearning(simulation);
		child.init(environment, location, this, otherParent);
		return child;
	}

	public void init(ComplexEnvironment env, LocationDirection pos, ComplexAgentParams agentData,
			LearningAgentParams lAgentData) {
		super.init(env, pos, agentData);

		lParams = lAgentData;
	}

	private void init(ComplexEnvironment env, LocationDirection pos, ComplexAgentLearning parent1, ComplexAgentLearning parent2) {
		super.init(env, pos, parent1, parent2);

		if (getRandom().nextBoolean()) {
			lParams = parent1.lParams;
		} else {
			lParams = parent2.lParams;
		}


	}

	private void init(ComplexEnvironment env, LocationDirection pos, ComplexAgentLearning parent) {
		super.init(env, pos, parent);

		lParams = parent.lParams;
	}


	/**
	 * A call to this method will cause an agent to scan the area around it for occurences that have
	 * happened to other agents. This is heavily influenced by the agent's learning parameters. The
	 * method will immediatly return if the agent is not set to learn from other agents. An agent will
	 * disregard occurrences that have happened to agents of a different type if it is not set to
	 * learn from dissimilar agents. The agent must be within an Occurrences's observeableDistance in
	 * order to process it.
	 */
	private void observeOccurrences() {
		if (!lParams.learnFromOthers) {
			return;
		}

		Location loc = this.getPosition();

		List<Occurrence> newOccList = new LinkedList<Occurrence>();

		for (Occurrence oc : allOccurrences) {
			if (oc.time - currTick >= 0) {
				ComplexAgentLearning occTarget = oc.target;
				Location loc2 = occTarget.getPosition();
				if (environment.topology.getDistance(loc,loc2) <= oc.detectableDistance
						&& (lParams.learnFromDifferentOthers || occTarget.getType() == getType())) {

					Direction directionTo = environment.topology.getDirectionBetween4way(loc, loc2);
					if (!directionTo.equals(Topology.NONE)) {
						String desc = null;
						Rotation rotationTo = environment.topology.getRotationBetween(getPosition().direction, directionTo);
						if (rotationTo == Rotation.Left) {
							desc = "turnLeft";
						}
						else if (rotationTo == Rotation.Right) {
							desc = "turnRight";
						}


						if (desc != null && oc.hasOccurred() && oc.getEvent() != null) {
							remember(new MemorableEvent(currTick, oc.getEvent().getMagnitude(), desc){
								//This information applies to only the present step the agent is about to take;
								//it will be irrelevant in the future (because new occurrences will be present)
								@Override
								public boolean forgetAfterStep() {
									return true;
								}
							});
						}
					}

				}
				newOccList.add(oc);
			}
		}

		allOccurrences = newOccList;
	}

	private void purgeMemory() {
		if (memEvents != null) {
			List<MemorableEvent> newMemEvents = new LinkedList<MemorableEvent>();
			for (MemorableEvent me : memEvents) {
				if (!me.forgetAfterStep()) {
					newMemEvents.add(me);
				}
			}
			memEvents = newMemEvents;
		}
	}


	public void setBreedPos(LocationDirection breedPos) {
		this.breedPos = breedPos;
	}


	public LocationDirection getBreedPos() {
		return breedPos;
	}

	@Override
	public void turnLeft() {
		//Impulse to turn left; may or may not do so based on its memories

		queue(new SmartAction(this, "turnLeft") {
			@Override
			public void desiredAction() {
				ComplexAgentLearning.super.turnLeft();
			}

			@Override
			public boolean eventIsRelated(MemorableEvent event) {
				return event.getDescription().substring(0, 4).equalsIgnoreCase("turn");
			}

			@Override
			public float getMagnitudeFromEvent(MemorableEvent event) {
				//If memory has to do with turning right, the opposite sign of the magnitude of that
				//event applies (if it is good to turn LEFT, it is bad to turn RIGHT sorta logic)
				if (event.getDescription().equals("turnRight")) {
					return event.getMagnitude() * -0.5f;
				}
				return super.getMagnitudeFromEvent(event);
			}

		});

	}

	@Override
	public void update(long tick) {
		if (!isAlive()) {
			return;
		}
		observeOccurrences();
		super.update(tick);
		performQueuedActions();
	}

	@Override
	public void turnRight() {
		//Impulse to turn right; may or may not do so based on its memories
		// Queue an action instead of executing it directly
		queue(new SmartAction(this, "turnRight") {
			@Override
			public void desiredAction() {
				ComplexAgentLearning.super.turnRight();
			}

			@Override
			public boolean eventIsRelated(MemorableEvent event) {
				return event.getDescription().substring(0, 4).equalsIgnoreCase("turn");
			}

			@Override
			public float getMagnitudeFromEvent(MemorableEvent event) {
				//If memory has to do with turning left, the opposite sign of the magnitude of that
				//event applies (if it is good to turn RIGHT, it is bad to turn LEFT.)
				if (event.getDescription().equals("turnLeft")) {
					return event.getMagnitude() * -0.5f;
				}
				return super.getMagnitudeFromEvent(event);
			}
		});
	}

	protected void performQueuedActions() {
		/*
		 * Perform all queued actions
		 */
		if (queueables != null && !queueables.isEmpty()) {
			// Use a second Collection to avoid concurrent modification issues
			List<Queueable> queueablesCopy = new LinkedList<Queueable>();
			queueablesCopy.addAll(queueables);
			queueables.clear();
			/*
			 * queueables is never iterated therefore a queueable may internally
			 * add new queueables to the queue without concurrently modifying
			 */
			for (Queueable act : queueablesCopy) {
				act.happen();
				if (!act.isComplete()) {
					/*
					 * The action is not complete therefore it will be performed
					 * again at the next tickNotification
					 */
					queueables.add(act);
				}
			}
			// Actions are essentially "queued" so once they are irrelevant they
			// are "forgotten"
		}

		purgeMemory();

	}
}
