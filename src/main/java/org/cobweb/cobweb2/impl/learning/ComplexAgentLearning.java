package org.cobweb.cobweb2.impl.learning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Direction;
import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.Rotation;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.cobweb2.plugins.broadcast.BroadcastPacket;
import org.cobweb.cobweb2.plugins.broadcast.FoodBroadcast;

//Food storage
//Vaccination/avoid infected agents
//Wordbuilding

//Make learning toggleable
//React to temperatures

// FIXME: untangle this mess, split out ComplexAgent's functions this duplicates and override them, use Cause and similar
public class ComplexAgentLearning extends ComplexAgent {

	public ComplexAgentLearning(SimulationInternals sim, int type) { // NO_UCD (unused code) called through reflection
		super(sim, type);
	}

	private static final long serialVersionUID = 6166561879146733801L;

	protected ComplexEnvironmentLearning getEnvironment() {
		return (ComplexEnvironmentLearning) environment;
	}

	/**
	 * A collection of events in memory.
	 */
	public List<MemorableEvent> memEvents = new LinkedList<MemorableEvent>();


	private List<Queueable> queueables = new LinkedList<Queueable>();

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

		memEvents.add(event);
		if (memEvents.size() > lParams.numMemories) {
			memEvents.remove(0);
		}
	}

	/**
	 * Events are queued using this method
	 * @param act The action to add to the queue.
	 */
	protected void queue(Queueable act) {
		if (act == null) {
			return;
		}

		queueables.add(act);
	}


	private LocationDirection breedPos = null;


	public LearningAgentParams lParams;

	@Override
	public void broadcast(BroadcastPacket packet) { // []SK
		super.broadcast(packet);

		// Deduct broadcasting cost from energy
		if (getEnergy() > 0) {
			// If still alive, the agent remembers that it is displeasing to
			// lose energy
			// due to broadcasting
			float howSadThisMakesMe = Math.max(Math.min(-params.broadcastEnergyCost / (float) getEnergy(), 1), -1);
			remember(new MemorableEvent(getTime(), howSadThisMakesMe, "broadcast"));
		}
	}

	@Override
	public void eat(Location destPos) {
		if (environment.getFoodType(destPos) == getType()) {
			// Eating food is ideal!!
			remember(new MemorableEvent(getTime(), lParams.foodPleasure, "food"));
		} else {
			// Eating other food has a ratio of goodness compared to eating
			// normal food.
			float howHappyThisMakesMe = (float) params.otherFoodEnergy / (float) params.foodEnergy
					* lParams.foodPleasure;
			remember(new MemorableEvent(getTime(), howHappyThisMakesMe, "food"));
		}

		super.eat(destPos);
	}

	@Override
	protected void eat(ComplexAgent adjacentAgent) {
		super.eat(adjacentAgent);
		// Bloodily consuming agents makes us happy
		remember(new MemorableEvent(getTime(), lParams.ateAgentPleasure, "ateAgent"));
	}

	@Override
	public void rememberBadAgent(ComplexAgent cheater) {
		super.rememberBadAgent(cheater);
		remember(new MemorableEvent(getTime(), -1, "agent-" + cheater.id));
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
						if (canBroadcast()) {
							agent.broadcast(new FoodBroadcast(destPos, agent));

							// Remember a sense of pleasure from helping out
							// other agents by broadcasting
							agent.remember(new MemorableEvent(getTime(), lParams.broadcastPleasure, "broadcast"));
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

				if (pregnant && enoughEnergy(params.breedEnergy) && pregPeriod <= 0) {

					queue(new BreedInitiationOccurrence(this, getTime(), 0, breedPartner == null ? new AsexualReproductionCause() : new SexualReproductionCause() , breedPartner));

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

			// Move the agent to destPos
			queue(new SmartAction(this, "move-" + destPos.toString()) {

				@Override
				public void desiredAction() {
					agent.move(destPos);
				}
			});

			// Try to breed
			queue(new Occurrence(this, getTime(), 0, "breed") {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					if (concernedAgent.getBreedPos() != null) {
						ComplexAgentLearning child = null;
						ReproductionCause cause = null;
						if (concernedAgent.breedPartner == null) {
							child = concernedAgent.createChildAsexual(concernedAgent.getBreedPos());
							cause = new AsexualReproductionCause();
						} else {
							child = concernedAgent.createChildSexual(
									concernedAgent.getBreedPos(),
									(ComplexAgentLearning)concernedAgent.breedPartner);

							cause = new SexualReproductionCause();

							// We like the agent we are breeding with; remember
							// that
							// this agent is favourable
							concernedAgent.remember(new MemorableEvent(getTime(), lParams.loveForPartner, "agent-" + breedPartner.id));

						}
						concernedAgent.changeEnergy(-params.initEnergy, cause);

						// Retain an undying feeling of love for our
						// child
						MemorableEvent weLoveOurChild = new MemorableEvent(getTime(), lParams.emotionForChildren, "" + child);
						concernedAgent.remember(weLoveOurChild);

						if (concernedAgent.breedPartner != null) {
							((ComplexAgentLearning)concernedAgent.breedPartner).remember(weLoveOurChild);
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
			queue(new EnergyChangeOccurrence(this, getTime(), -params.stepEnergy, new StepForwardCause()) {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					MemorableEvent ret = super.effect(concernedAgent);

					return ret;
				}
			});

		} else if ((adjAgent = getAdjacentAgent()) != null && adjAgent instanceof ComplexAgentLearning) {
			// two agents meet

			final ComplexAgentLearning adjacentAgent = (ComplexAgentLearning) adjAgent;

			queue(new Occurrence(this, getTime(), 0, "contactMutate") {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					getAgentListener().onContact(concernedAgent, adjacentAgent);
					return null;
				}
			});

			if (canEat(adjacentAgent)) {
				//An action to conditionally eat the agent
				queue(new SmartAction(this, "agent-" + adjacentAgent.id) {

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
						adjacentAgent.remember(new MemorableEvent(getTime(), lParams.sparedEmotion, "agent-" + id));
					}
				});
			}

			// if the agents are of the same type, check if they have enough
			// resources to breed
			if (adjacentAgent.getType() == getType()) {

				double sim = 0.0;
				boolean canBreed = !pregnant && enoughEnergy(params.breedEnergy) && params.sexualBreedChance != 0.0
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
						&& isAgentGood(adjacentAgent) && adjacentAgent.isAgentGood(this)) {
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

			changeEnergy(-params.stepAgentEnergy, new StepForwardCause());

		} // end of two agents meet
		else if (destPos != null && environment.hasDrop(destPos)) {

			Drop d = environment.getDrop(destPos);
			if (!d.canStep()) {

				// Allow agents up to a distance of 5 to see this agent hit the
				// waste
				queue(new Occurrence(this, getTime(), 5, "Bump Wall") {

					@Override
					public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
						concernedAgent.queue(new EnergyChangeOccurrence(concernedAgent, time, -params.stepRockEnergy, new BumpWallCause()));
						return null;
					}
				});
			}

		} else {
			// Rock bump
			queue(new Occurrence(this, getTime(), 0, "Bump Wall") {

				@Override
				public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
					concernedAgent
					.queue(new EnergyChangeOccurrence(concernedAgent, time, -params.stepRockEnergy, new BumpWallCause()));
					return null;
				}
			});
		}

		// Energy penalty
		if (energyPenalty() > 0)
			queue(new EnergyChangeOccurrence(this, getTime(), -energyPenalty(), new AgingPenaltyCause()));

		if (getEnergy() <= 0)
			queue(new SmartAction(this) {

				@Override
				public void desiredAction() {
					agent.die();
				}
			});

		if (getEnergy() < params.breedEnergy) {
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
			queue(new Occurrence(this, getTime(), 0, "preg") {

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
		ComplexAgentLearning child = new ComplexAgentLearning(simulation, getType());
		child.init(environment, location, this);
		return child;
	}

	private ComplexAgentLearning createChildSexual(LocationDirection location, ComplexAgentLearning otherParent) {
		ComplexAgentLearning child = new ComplexAgentLearning(simulation, getType());
		child.init(environment, location, this, otherParent);
		return child;
	}

	private void init(ComplexEnvironment env, LocationDirection pos, ComplexAgentLearning parent1, ComplexAgentLearning parent2) {
		super.init(env, pos, parent1, parent2);

		if (getRandom().nextBoolean()) {
			lParams = parent1.lParams.clone();
		} else {
			lParams = parent2.lParams.clone();
		}


	}

	private void init(ComplexEnvironment env, LocationDirection pos, ComplexAgentLearning parent) {
		super.init(env, pos, parent);

		lParams = parent.lParams.clone();
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

		for (Occurrence oc : getEnvironment().allOccurrences)
		{
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
						remember(new MemorableEvent(getTime(), oc.getEvent().getMagnitude(), desc){
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

		}

	}

	private void pruneMemory() {
		// Clean up events
		Iterator<MemorableEvent> events = memEvents.iterator();
		while (events.hasNext()) {
			MemorableEvent me = events.next();
			if (me.forgetAfterStep())
				events.remove();
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
	public void update() {
		if (!isAlive()) {
			return;
		}
		observeOccurrences();
		super.update();
		performQueuedActions();
		pruneMemory();
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

	/*
	 * Perform all queued actions
	 */
	protected void performQueuedActions() {
		// Copy current list, some actions could queue new actions
		List<Queueable> currentActions = new ArrayList<Queueable>(queueables);

		// Clear current list, we will re-add any left-over actions later
		queueables.clear();

		Iterator<Queueable> iterator = currentActions.iterator();
		while (iterator.hasNext()) {
			Queueable act = iterator.next();

			act.happen();

			if (act.isComplete())
				iterator.remove();
		}

		// Re-add actions that did not complete on this turn
		queueables.addAll(currentActions);
	}
}
