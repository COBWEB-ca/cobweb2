package cwcore;

import ga.GATracker;
import ga.GeneticCode;
import ga.GeneticCodeException;
import ga.PhenotypeMaster;

import java.awt.Color;
import java.awt.Point;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import cobweb.ColorLookup;
import cobweb.TypeColorEnumeration;
import cobweb.globals;
import cwcore.ComplexEnvironment.CommManager;
import cwcore.ComplexEnvironment.CommPacket;
import driver.ControllerFactory;

public class ComplexAgent extends cobweb.Agent implements
		cobweb.TickScheduler.Client {

	/** Default mutable parameters of each agent type. */
	private static float[] default_mutationRate = new float[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_energy = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_foodEnergy = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_otherFoodEnergy = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_breedEnergy = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_pregnancyPeriod = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_stepEnergy = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_stepRockEnergy = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_turnRightEnergy = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_turnLeftEnergy = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_commSimMin = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_stepAgentEnergy = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_sexualPregnancyPeriod = new int[ComplexEnvironment.AGENT_TYPES];
	private static float[] default_breedSimMin = new float[ComplexEnvironment.AGENT_TYPES];
	private static float[] default_sexualBreedChance = new float[ComplexEnvironment.AGENT_TYPES];
	private static float[] default_asexualBreedChance = new float[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_agingLimit = new int[ComplexEnvironment.AGENT_TYPES];
	private static float[] default_agingRate = new float[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_pdCoopProb = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_broadcastFixedRange = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_broadcastEnergyMin = new int[ComplexEnvironment.AGENT_TYPES];
	private static int[] default_broadcastEnergyCost = new int[ComplexEnvironment.AGENT_TYPES];

	/** Sets the default mutable parameters of each agent type. */
	public static void setDefaultMutableParams(float[] d_mutationRate,
			int[] d_energy, int[] d_foodEnergy, int[] d_otherFoodEnergy,
			int[] d_breedEnergy, int[] d_pregnancyPeriod, int[] d_stepEnergy,
			int[] d_stepRockEnergy, int[] d_turnRightEnergy,
			int[] d_turnLeftEnergy, int[] d_commSimMin,
			int[] d_stepAgentEnergy, int[] d_sexualPregnancyPeriod,
			float[] d_breedSimMin, float[] d_sexualBreedChance,
			float[] d_asexualBreedChance, int[] d_agingLimit,
			float[] d_agingRate, int[] d_pdCoopProb,
			int[] d_broadcastFixedRange, int[] d_broadcastEnergyMin,
			int[] d_broadcastEnergyCost) {

		for (int i = 0; i < ComplexEnvironment.AGENT_TYPES; i++) {
			default_mutationRate[i] = d_mutationRate[i];
			default_energy[i] = d_energy[i];
			default_foodEnergy[i] = d_foodEnergy[i];
			default_otherFoodEnergy[i] = d_otherFoodEnergy[i];
			default_breedEnergy[i] = d_breedEnergy[i];
			default_pregnancyPeriod[i] = d_pregnancyPeriod[i];
			default_stepEnergy[i] = d_stepEnergy[i];
			default_stepRockEnergy[i] = d_stepRockEnergy[i];
			default_turnRightEnergy[i] = d_turnRightEnergy[i];
			default_turnLeftEnergy[i] = d_turnLeftEnergy[i];
			default_commSimMin[i] = d_commSimMin[i];
			default_stepAgentEnergy[i] = d_stepAgentEnergy[i];
			default_sexualPregnancyPeriod[i] = d_sexualPregnancyPeriod[i];
			default_breedSimMin[i] = d_breedSimMin[i];
			default_sexualBreedChance[i] = d_sexualBreedChance[i];
			default_asexualBreedChance[i] = d_asexualBreedChance[i];
			default_agingLimit[i] = d_agingLimit[i];
			default_agingRate[i] = d_agingRate[i];
			default_pdCoopProb[i] = d_pdCoopProb[i];
			default_broadcastFixedRange[i] = d_broadcastFixedRange[i];
			default_broadcastEnergyMin[i] = d_broadcastEnergyMin[i];
			default_broadcastEnergyCost[i] = d_broadcastEnergyCost[i];
		}

	}

	// private long id;
	@Override
	public String getID() {
		return "" + id;
	}

	private int agentType;

	@Override
	public int type() {
		return agentType;
	}

	/* energy gauge */
	private int energy;

	// return agent's energy
	@Override
	public int getEnergy() {
		return energy;
	}

	/* energy gained from food */
	private int foodEnergy;
	/* energy gained from other non-food sources */
	private int otherFoodEnergy;
	/* energy treshold required for breeding */
	private int breedEnergy;
	/* energy consumed initially? After giving birth? */
	private int initEnergy; // $$$$$ "Initial Energy"
	/* energy consumed per step */
	private int stepEnergy;
	/* energy consumed when stepping on a rock */
	private int stepRockEnergy;
	/* energy consumed when bumping another agent */
	private int stepAgentEnergy;

	/** The genetic code of the agent. Default is 24 bit. */
	private GeneticCode genetic_code;

	/** Returns the genetic code of the agent. Default is 24 bit. */
	@Override
	public GeneticCode getGeneticCode() {
		return genetic_code;
	}

	/** Returns the genetic sequence of the agent. Default is 24 bit. */
	@Override
	public String getGeneticSequence() {
		return genetic_code.getGeneticInfo();
	}

	/* Prisoner's Dilemma */
	private int agentPDStrategy; // tit-for-tat or probability
	private int agentPDAction; // The agent's action; 1 == cheater, else
	// cooperator
	private int lastPDMove; // Remember the opponent's move in the last game

	/* energy consumed when turning */
	private int turnLeftEnergy;
	private int turnRightEnergy;
	/* */
	private float mutationRate;

	private boolean colorful;
	private int commSimMin;
	private int commInbox;
	private int commOutbox;
	// memory size is the maximum capacity of the number of cheaters an agent
	// can remember
	private int memory_size;
	private int photo_memory[];
	private int photo_num = 0;
	private boolean want2meet = false;
	private boolean food_bias = false;
	boolean cooperate;

	private int plantstoeat[] = new int[10];
	private int agentstoeat[] = new int[10];

	/* self explanatory */
	private float sexualBreedChance;
	private float asexualBreedChance;

	/* Aging variables */
	private boolean agingMode;
	private int agingLimit;
	private float agingRate = 1;
	private long birthTick = 0;

	@Override
	public long birthday() {
		return birthTick;
	}

	private long age = 0;

	@Override
	public long age() {
		return age;
	}

	/* Waste variables */
	private boolean wasteMode;
	private int wastePen;
	private int wasteGain;
	private int wasteLoss;
	private float wasteRate;
	private int wasteInit;

	private int _wasteGain;
	private int _wasteLoss;

	/* PD variables */
	private boolean pdTitForTat;
	private int pdCoopProb;

	/* Broadcast variables */
	private boolean broadcastMode;
	private boolean broadcastEnergyBased;
	private int broadcastFixedRange;
	private int broadcastEnergyMin;
	private int broadcastEnergyCost;

	private float breedSimMin;
	private int sexualPregnancyPeriod;
	private int memoryBuffer;

	private ComplexAgent breedPartner;

	private Color color;

	private boolean asexFlag;

	private ComplexAgentInfo info;

	public ComplexAgentInfo getInfo() {
		return info;
	}

	// pregnancyPeriod is set value while pregPeriod constantly changes
	private int pregnancyPeriod;
	private int pregPeriod;
	private boolean pregnant = false;

	private static boolean tracked = false;

	private double energyPenalty(boolean log) {
		if (!agingMode)
			return 0.0;
		Double age = new Double(currTick - birthTick);
		if (tracked && log)
			info.useExtraEnergy(
				agentType,
				Math.min(Math.max(0, energy),
				(int) (agingRate * (Math.tan(((age.doubleValue() / agingLimit) * 89.99)
						* Math.PI / 180)) + 0.5)));
		return Math.min(Math.max(0, energy), agingRate
				* (Math.tan(((age.doubleValue() / agingLimit) * 89.99)
						* Math.PI / 180)));
	}

	public static void clearData() {
		if (tracked)
			ComplexAgentInfo.resetGroupData();
	}

	public static void dumpData(long tick) {
		if (tracked)
			ComplexAgentInfo.dumpGroupData(tick, writer);
	}

	// static-izing the writer. Doesn't seem feasible to have a writer for each
	// agent.
	// main issue is speed and also the need to find a graceful way to produce
	// the output
	// (dumping 200 .txt files != graceful).
	private static java.io.Writer writer;

	// static-izing this too. see the comment above
	public static void tracked() {
		tracked = true;
	}

	/* The current tick we are in (or the last tick this agent was notified */
	private long currTick = 0;

	public static void setPrintWriter(java.io.Writer w) {
		writer = w;
	}

	private static ColorLookup colorMap = TypeColorEnumeration.getInstance();

	@Override
	public void die() {
		super.die();
		GATracker.removeAgent(this); // Remove this agent from the
										// GATracker's list of agent (presumably
										// all living agents)

		info.setDeath(((ComplexEnvironment) position.getEnvironment())
				.getTickCount());
	}

	// get agent's drawing information given the UI
	@Override
	public void getDrawInfo(cobweb.UIInterface theUI) {
		Color stratColor;

		// is agents action is 1, it's a cheater therefore it's
		// graphical representation will a have red boundary
		if (agentPDAction == 1) {
			stratColor = Color.red;
		} else {
			// cooperator, black boundary
			stratColor = Color.black;
		}
		// based on agent type
		if (colorful) {
			theUI.newAgent(getColor(), colorMap.getColor(agentType, 1),
					stratColor, new Point(getPosition().v[0],
							getPosition().v[1]), new Point(
							facing.v[0], facing.v[1]));
		} else {

			theUI.newAgent(Color.red, stratColor, new Point(
					getPosition().v[0], getPosition().v[1]),
					new Point(facing.v[0], facing.v[1]));
		}
	}

	static class lookPair {
		public lookPair(int d, int t) {
			dist = d;
			type = t;
		}

		public int getDist() {
			return dist;
		}

		public int getType() {
			return type;
		}

		private int dist;
		private int type;
	}

	/*
	 * return the measure of similiarity between this agent and the 'other'
	 * ranging from 0.0 to 1.0 (identical)
	 */
	@Override
	public double similarity(cobweb.Agent other) {
		if (!(other instanceof ComplexAgent))
			return 0.0;
		return //((GeneticController) controller)
				//.similarity((GeneticController) ((ComplexAgent) other)
					//	.getController());
			((LinearWeightsController) controller)
			.similarity((LinearWeightsController) other.getController());
	}

	@Override
	public double similarity(int other) {
		return 0.5; //((GeneticController) controller).similarity(other);
	}

	@Override
	public void setColor(Color c) {
		color = c;
	}

	@Override
	public Color getColor() {
		return color;
	}

	public long getAge() {
		return age;
	}

	public int getCommInbox() {
		return commInbox;
	}

	public void setCommInbox(int commInbox) {
		this.commInbox = commInbox;
	}

	public int getCommOutbox() {
		return commOutbox;
	}

	public void setCommOutbox(int commOutbox) {
		this.commOutbox = commOutbox;
	}

	public int getMemoryBuffer() {
		return memoryBuffer;
	}

	public void setMemoryBuffer(int memoryBuffer) {
		this.memoryBuffer = memoryBuffer;
	}

	public boolean isAsexFlag() {
		return asexFlag;
	}

	public void setAsexFlag(boolean asexFlag) {
		this.asexFlag = asexFlag;
	}

	@SuppressWarnings("unused")
	private static long groupLastPolled = 1;

	/*
	 * Record the state here for logging. Can also be treated as a "setup"
	 * function before doing any activities. Might be useful in the future. This
	 * is only run when tracking is enabled.
	 */
	private void poll() {
		info.addEnergy(agentType, energy);
		info.alive(agentType);
	}

	public void tickNotification(long tick) {
		/* The current tick */
		currTick = tick;

		/* Hack to find the birth tick... */
		if (birthTick == 0)
			birthTick = currTick;
		age++;

		/* Time to die, Mr. Bond */
		if (agingMode) {
			if ((currTick - birthTick) >= agingLimit) {
				die();
				return;
			}
		}

		/* Move/eat/reproduce/etc */
		controller.controlAgent(this);

		/* track me */
		if (tracked)
			poll();

		/* Produce waste if able */
		if (wasteMode)
			poop();

		/* Check if broadcasting is enabled */
		if (broadcastMode & !ComplexEnvironment.currentPackets.isEmpty())
			receiveBroadcast();// []SK
	}

	public long look() {
		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
		// If the position is invalid, then we're looking at a stone...
		if (destPos == null)
			return ComplexEnvironment.FLAG_STONE;
		// Check for stone...
		if (destPos.testFlag(ComplexEnvironment.FLAG_STONE))
			return ComplexEnvironment.FLAG_STONE;
		// If there's another agent there, then return that it's a stone...
		if (destPos.getAgent() != null)
			return ComplexEnvironment.FLAG_STONE;
		// If there's food there, return the food...
		if (destPos.testFlag(ComplexEnvironment.FLAG_FOOD))
			return ComplexEnvironment.FLAG_FOOD;
		// waste check
		if (destPos.testFlag(ComplexEnvironment.FLAG_WASTE))
			return ComplexEnvironment.FLAG_WASTE;

		// Return an empty tile
		return 0;
	}

	public static final int LOOK_DISTANCE = 4;

	public lookPair distanceLook() {
		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
		for (int dist = 1; dist <= LOOK_DISTANCE; ++dist) {

			// We are looking at the wall
			if (destPos == null)
				return new lookPair(dist, ComplexEnvironment.FLAG_STONE);

			// Check for stone...
			if (destPos.testFlag(ComplexEnvironment.FLAG_STONE))
				return new lookPair(dist, ComplexEnvironment.FLAG_STONE);

			// If there's another agent there, then return that it's a stone...
			if (destPos.getAgent() != null && destPos.getAgent() != this)
				return new lookPair(dist, ComplexEnvironment.FLAG_AGENT);

			// If there's food there, return the food...
			if (destPos.testFlag(ComplexEnvironment.FLAG_FOOD))
				return new lookPair(dist, ComplexEnvironment.FLAG_FOOD);

			if (destPos.testFlag(ComplexEnvironment.FLAG_WASTE))
				return new lookPair(dist, ComplexEnvironment.FLAG_WASTE);

			destPos = destPos.getAdjacent(facing);
		}
		return new lookPair(LOOK_DISTANCE, 0);
	}

	boolean canStep() {
		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
		// The position must be valid...
		if (destPos == null)
			return false;
		// and the destination must be clear of stones
		if (destPos.testFlag(ComplexEnvironment.FLAG_STONE))
			return false;
		// and clear of wastes
		if (destPos.testFlag(ComplexEnvironment.FLAG_WASTE))
			return false;
		// as well as other agents...
		if (destPos.getAgent() != null)
			return false;
		return true;
	}

	cobweb.Agent getAdjacentAgent() {
		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
		if (destPos == null) {
			return null;
		}
		return destPos.getAgent();
	}

	void step() {
		cobweb.Agent adjAgent;

		if (canStep()) {
			// Check for food...
			cobweb.Environment.Location destPos = getPosition().getAdjacent(
					facing);
			cobweb.Environment.Location breedPos = null;
			if (destPos.testFlag(ComplexEnvironment.FLAG_FOOD)) {
				if (broadcastMode & canBroadcast()) {
					// System.out.println("Agent " + getID() + " found food at
					// location: " + destPos
					// + ", energy: " + energy);
					broadcastFood(destPos);
				}
				if (canEat(destPos)) {
					eat(destPos);
				}
				if (pregnant && energy >= breedEnergy && pregPeriod <= 0) {

					breedPos = getPosition();
					energy -= initEnergy;
					energy -= energyPenalty(true);
					_wasteLoss -= initEnergy;
					info.useOthers(agentType, initEnergy);

				} else if (!pregnant
						&& asexFlag
						&& energy >= breedEnergy
						&& asexualBreedChance != 0.0
						&& cobweb.globals.random.nextFloat() < asexualBreedChance) {
					pregPeriod = pregnancyPeriod;
					pregnant = true;
				}
			}

			move(getPosition().getAdjacent(facing));
			if (breedPos != null) {

				if (breedPartner == null) {
					info.addDirectChild();
					new ComplexAgent(breedPos, this, this.agentPDAction);
				} else {
					// child's strategy is determined by its parents, it has a
					// 50%
					// chance to get either
					// parent's strategy
					int childStrategy = -1;
					if (this.agentPDAction != -1) {
						boolean choose = globals.random.nextBoolean();
						if (choose) {
							childStrategy = this.agentPDAction;
						} else {
							childStrategy = breedPartner.agentPDAction;
						}
					}

					info.addDirectChild();
					breedPartner.info.addDirectChild();
					new ComplexAgent(breedPos, this, breedPartner,
							childStrategy);
					info.addSexPreg();
				}
				breedPartner = null;
				pregnant = false;
			}
			energy -= stepEnergy;
			energy -= energyPenalty(true);
			_wasteLoss -= stepEnergy;
			info.useStepEnergy(agentType, stepEnergy);
			info.addStep();
			info.addPathStep(this.getPosition());

		} else if ((adjAgent = getAdjacentAgent()) != null
				&& adjAgent instanceof ComplexAgent) {
			// two agents meet

			ComplexAgent adjacentAgent = (ComplexAgent) adjAgent;

			if (canEat(adjacentAgent)) {
				eat(adjacentAgent);
			}

			if (this.agentPDAction != -1) {// $$$$$ if playing Prisoner's
											// Dilemma. Please refer to
											// ComplexEnvironment.load, "//
											// spawn new random agents for each
											// type"
				want2meet = true;
			}

			int othersID = adjacentAgent.info.getAgentNumber();
			// scan the memory array, is the 'other' agents ID is found in the
			// array, then
			// choose not to have a transaction with him.
			for (int i = 0; i < photo_num; i++) {
				if (photo_memory[i] == othersID) {
					want2meet = false;
				}
			}
			// if the agents are of the same type, check if they have enough
			// resources to breed
			if (adjacentAgent.agentType == agentType) {

				double sim = 0.0;
				boolean canBreed = !pregnant
						&& energy >= breedEnergy
						&& sexualBreedChance != 0.0
						&& cobweb.globals.random.nextFloat() < sexualBreedChance;

				// Generate genetic similarity number
				try {
					sim = GeneticCode.compareGeneticSimilarity(this
							.getGeneticSequence(), adjacentAgent
							.getGeneticSequence());
				} catch (GeneticCodeException e) {
					System.err.println(e.getMessage());
				}

				if (sim >= commSimMin) {
					communicate(adjacentAgent);
				}

				if (canBreed
						&& sim >= breedSimMin
						&& ((want2meet && adjacentAgent.want2meet) || (agentPDAction == -1))) {
					pregnant = true;
					pregPeriod = sexualPregnancyPeriod;
					breedPartner = adjacentAgent;
				}
			}
			// perform the transaction only if non-pregnant and both agents want
			// to
			// meet
			if (!pregnant && want2meet && adjacentAgent.want2meet) {

				// cobweb.UIInterface ui = ComplexEnvironment.getUIPipe(); //
				// $$$$$$ this silenced line was for writing PD info into the
				// output window. Apr 22

				// int strat = 0; // $$$$$$ silenced on Apr 22

				playPD();
				adjacentAgent.playPD();

				lastPDMove = adjacentAgent.agentPDAction; // Adjacent Agent's
															// action
				// is assigned to last move
				// memory of the agent
				adjacentAgent.lastPDMove = agentPDAction; // Agent's action is
				// assigned to the last move
				// memory of the adjacent agent

				/**
				 * *** $$$$$$ This silenced block was used for updating PD info
				 * in the output window. Apr 22 String Strategy = null; // $$$$$
				 * this String used to be used for the Output Window's updating.
				 * Apr 18 if (pdTitForTat) { // remove this if-block later
				 * //agentPDStrategy = 1; Strategy = "TitForTat"; } else {
				 * //agentPDStrategy = 0; Strategy = "Probability"; }
				 *
				 *
				 * String adjStrategy = null; // $$$$$ this String used to be
				 * used for the Output Window's updating. Apr 18 if
				 * (adjacentAgent.pdTitForTat) { // remove this if-block later
				 * //adjacentAgent.agentPDStrategy = 1; adjStrategy =
				 * "TitForTat"; } else { //adjacentAgent.agentPDStrategy = 0;
				 * adjStrategy = "Probability"; }
				 */

				/*
				 * TODO The ability for the PD game to contend for the Get the
				 * food tiles immediately around each agents: . . . . . . . . . . . . . . . . . . .
				 * X X X X . . . > < . . -> . X > < X . . . . . . . . X X X X . . . . . . . . . . . . .
				 *
				 * (including the two under the agents!)
				 */

				/* 0 = cooperate. 1 = defect */

				/*
				 * Payoff Matrix: 0 0 => 5 5 0 1 => 2 8 1 0 => 8 2 1 1 => 3 3
				 */

				@SuppressWarnings("unused")
				final int PD_STATIC_PAYOFF = 0;

				final int PD_COOPERATE = 0;
				final int PD_DEFECT = 1;

				/*
				 * ui.writeToTextWindow("PD triggered between agents
				 * ("+position.v[0]+","+position.v[1]+") and ("+
				 * adjacentAgent.getPosition().v[0]+","+adjacentAgent.getPosition().v[1]+"):\n");
				 */

				/* REWARD */
				if (agentPDAction == PD_COOPERATE
						&& adjacentAgent.agentPDAction == PD_COOPERATE) {
					energy += ComplexEnvironment.PD_PAYOFF_REWARD;
					adjacentAgent.energy += ComplexEnvironment.PD_PAYOFF_REWARD;

					/*
					 * ui.writeToTextWindow(" Agent "+getID()+"
					 * ("+position.v[0]+","+position.v[1]+") COOPERATED and
					 * gained "+ComplexEnvironment.PD_PAYOFF_REWARD+" energy
					 * unit(s)\n"); ui.writeToTextWindow(" Type: "+(type()+1)+"
					 * Strategy: "+Strategy+"\n"); ui.writeToTextWindow(" Agent
					 * "+(adjacentAgent.getID())+"
					 * ("+adjacentAgent.getPosition().v[0]+","+adjacentAgent.getPosition().v[1]+")
					 * COOPERATED and gained
					 * "+ComplexEnvironment.PD_PAYOFF_REWARD+" energy
					 * unit(s)\n"); ui.writeToTextWindow(" Type:
					 * "+(adjacentAgent.type()+1)+" Strategy:
					 * "+adjStrategy+"\n");
					 */

					/* SUCKER */
				} else if (agentPDAction == PD_COOPERATE
						&& adjacentAgent.agentPDAction == PD_DEFECT) {
					energy += ComplexEnvironment.PD_PAYOFF_SUCKER;
					adjacentAgent.energy += ComplexEnvironment.PD_PAYOFF_TEMPTATION;

					/*
					 * ui.writeToTextWindow(" Agent "+getID()+"
					 * ("+position.v[0]+","+position.v[1]+") COOPERATED and
					 * gained "+ComplexEnvironment.PD_PAYOFF_SUCKER+" energy
					 * unit(s)\n"); ui.writeToTextWindow(" Type: "+(type()+1)+"
					 * Strategy: "+Strategy+"\n"); ui.writeToTextWindow(" Agent
					 * "+(adjacentAgent.getID())+"
					 * ("+adjacentAgent.getPosition().v[0]+","+adjacentAgent.getPosition().v[1]+")
					 * DEFECTED and gained
					 * "+ComplexEnvironment.PD_PAYOFF_TEMPTATION+" energy
					 * unit(s)\n"); ui.writeToTextWindow(" Type:
					 * "+(adjacentAgent.type()+1)+" Strategy:
					 * "+adjStrategy+"\n");
					 */

					photo_memory[photo_num % memory_size] = othersID;

					broadcastCheating(getPosition());
					/* TEMPTATION */
				} else if (agentPDAction == PD_DEFECT
						&& adjacentAgent.agentPDAction == PD_COOPERATE) {
					energy += ComplexEnvironment.PD_PAYOFF_TEMPTATION;
					adjacentAgent.energy += ComplexEnvironment.PD_PAYOFF_SUCKER;

					/*
					 * ui.writeToTextWindow(" Agent "+getID()+"
					 * ("+position.v[0]+","+position.v[1]+") DEFECTED and gained
					 * "+ComplexEnvironment.PD_PAYOFF_TEMPTATION+" energy
					 * unit(s)\n"); ui.writeToTextWindow(" Type: "+(type()+1)+"
					 * Strategy: "+Strategy+"\n"); ui.writeToTextWindow(" Agent
					 * "+(adjacentAgent.getID())+"
					 * ("+adjacentAgent.getPosition().v[0]+","+adjacentAgent.getPosition().v[1]+")
					 * COOPERATED and gained
					 * "+ComplexEnvironment.PD_PAYOFF_SUCKER+" energy
					 * unit(s)\n"); ui.writeToTextWindow(" Type:
					 * "+(adjacentAgent.type()+1)+" Strategy:
					 * "+adjStrategy+"\n");
					 */
					/* PUNISHMENT */
				} else if (agentPDAction == PD_DEFECT
						&& adjacentAgent.agentPDAction == PD_DEFECT) {
					energy += ComplexEnvironment.PD_PAYOFF_PUNISHMENT;
					adjacentAgent.energy += ComplexEnvironment.PD_PAYOFF_PUNISHMENT; // $$$$$$
																						// change
																						// from
																						// "adjacentAgent.energy
																						// +=
																						// 3;"
																						// (3???)
																						// to
					// "adjacentAgent.energy +=
					// ComplexEnvironment.PD_PAYOFF_PUNISHMENT;" Apr 22
					/*
					 * ui.writeToTextWindow(" Agent "+getID()+"
					 * ("+position.v[0]+","+position.v[1]+") DEFECTED and gained
					 * "+ComplexEnvironment.PD_PAYOFF_PUNISHMENT+" energy
					 * unit(s)\n"); ui.writeToTextWindow(" Type: "+(type()+1)+"
					 * Strategy: "+Strategy+"\n"); ui.writeToTextWindow(" Agent
					 * "+(adjacentAgent.getID())+"
					 * ("+adjacentAgent.getPosition().v[0]+","+adjacentAgent.getPosition().v[1]+")
					 * DEFECTED and gained
					 * "+ComplexEnvironment.PD_PAYOFF_PUNISHMENT+" energy
					 * unit(s)\n"); ui.writeToTextWindow(" Type:
					 * "+(adjacentAgent.type()+1)+" Strategy:
					 * "+adjStrategy+"\n");
					 */

					photo_memory[photo_num % memory_size] = othersID;

					broadcastCheating(getPosition());
				}

				if (photo_num % memory_size == 0) {
					photo_num = 0;
				}

				photo_num++;

				/*
				 * boolean tempt = false; // energy of both agents is summed up
				 * for the transaction int Epool = energy +
				 * adjacentAgent.energy; // _wasteGain += adjacentAgent.energy;
				 * if(agentPDAction == 0) cooperate = true; else cooperate =
				 * false;
				 *
				 * if (adjacentAgent.energy > energy && food_bias){ tempt =
				 * true; if (agentPDAction == 0){ cooperate = true; float rnd =
				 * cobweb.globals.random.nextFloat(); if (rnd > 0.8) cooperate =
				 * false; }else if(agentPDAction == 1){ cooperate = false; float
				 * rnd = cobweb.globals.random.nextFloat(); if (rnd > 0.8)
				 * cooperate = true; } } // if agent is a cooperator if
				 * (cooperate ){ // both cooperate
				 * if(adjacentAgent.agentPDAction == 0){ energy =
				 * (int)(Epool/2); if ((energy - adjacentAgent.energy) >
				 * (int)(Epool/2)) { _wasteLoss -= ((energy -
				 * adjacentAgent.energy) - (int)(Epool/2));
				 * info.useOthers(agentType, ((energy - adjacentAgent.energy) -
				 * (int)(Epool/2))); } else { _wasteGain -= (int)(Epool/2) -
				 * (energy - adjacentAgent.energy) ; info.addOthers(agentType,
				 * ((energy - adjacentAgent.energy) - (int)(Epool/2))); } }else{
				 * energy -= (int)(energy/2); _wasteLoss -= (int)(energy/2);
				 * info.useOthers(agentType, (int)(energy/2)); //remeber the
				 * cheater photo_memory[photo_num % memory_size] = othersID; } }
				 * else if(!cooperate){ if(adjacentAgent.agentPDAction == 0){
				 * energy += (int)((adjacentAgent.energy)/2); _wasteGain -=
				 * (int)(adjacentAgent.energy/2); info.addOthers(agentType,
				 * (int)(adjacentAgent.energy/2)); }else{ energy -=
				 * (int)(energy*(0.75)); _wasteLoss -= (int)(energy*0.75);
				 * info.useOthers(agentType, (int)(energy*0.75));
				 * photo_memory[photo_num % memory_size] = othersID; } }
				 * if(photo_num % memory_size == 0){ photo_num = 0; }
				 * photo_num++;
				 *
				 */
			}
			energy -= stepAgentEnergy;
			energy -= energyPenalty(true);
			_wasteLoss -= stepAgentEnergy;
			info.useAgentBumpEnergy(agentType, stepAgentEnergy);
			info.addAgentBump();
		} // end of two agents meet

		else {
			energy -= stepRockEnergy;
			energy -= energyPenalty(true);
			_wasteLoss -= stepRockEnergy;
			info.useRockBumpEnergy(agentType, stepRockEnergy);
			info.addRockBump();
		}

		if (energy <= 0)
			die();

		if (energy < breedEnergy) {
			pregnant = false;
			breedPartner = null;
		}

		if (pregnant) {
			pregPeriod--;
		}
	}

	/**
	 * @author skinawy
	 * @return void This method determines the action of the agent in a PD game
	 */
	public void playPD() {

		double coopProb = pdCoopProb / 100.0d; // static value for now

		if (pdTitForTat) { // if true then agent is playing TitForTat
			agentPDStrategy = 1; // set Strategy to 1 i.e. TitForTat
			if (lastPDMove == -1)// if this is the first move
				lastPDMove = 0; // start by cooperating
			// might include probability bias within TitForTat strategy...not
			// currently implemented
			agentPDAction = lastPDMove;
		} else {
			agentPDStrategy = 0; // $$$$$$ added to ensure Strategy is set to
									// 0, i.e. probability. Apr 22
			agentPDAction = 0; // agent is assumed to cooperate
			float rnd = cobweb.globals.random.nextFloat();
			// System.out.println("rnd: " + rnd);
			// System.out.println("coopProb: " + coopProb);
			if (rnd > coopProb)
				agentPDAction = 1; // agent defects depending on
			// probability
		}

		return;
	}

	private boolean canBroadcast() {
		if (energy > broadcastEnergyMin)
			return true;
		return false;
	}

	private boolean canEat(ComplexAgent adjacentAgent) {
		boolean caneat = false;
		for (int element : agentstoeat) {
			if (element == adjacentAgent.getAgentType()) {
				caneat = true;
			}
		}
		if (this.energy > this.breedEnergy)
			caneat = false;

		return caneat;
	}

	public boolean canEat(cobweb.Environment.Location destPos) {
		boolean caneat = false;
		for (int element : plantstoeat) {
			if (element == ComplexEnvironment.getFoodType(destPos))
				caneat = true;
		}
		return caneat;
	}

	private cobweb.Environment.Direction[] dirList = {
			cobweb.Environment.DIRECTION_NORTH,
			cobweb.Environment.DIRECTION_SOUTH,
			cobweb.Environment.DIRECTION_WEST,
			cobweb.Environment.DIRECTION_EAST,
			cobweb.Environment.DIRECTION_NORTHEAST,
			cobweb.Environment.DIRECTION_SOUTHEAST,
			cobweb.Environment.DIRECTION_NORTHWEST,
			cobweb.Environment.DIRECTION_SOUTHWEST };

	/* Produce waste */
	private void poop(/* cobweb.Environment.Location l */) {
		// we should check before calling this function instead
		// if (!wasteMode) return;
		boolean produce = false;
		if (_wasteGain <= 0) {
			produce = true;
			_wasteGain += wasteGain;
		} else if (_wasteLoss <= 0) {
			produce = true;
			_wasteLoss += wasteLoss;
		}
		if (!produce)
			return;

		boolean wasteAdded = false;
		/* Output a waste somewhere "close" (rad 1 from currentPosition) */
		for (int i = 0; i < dirList.length; i++) {
			cobweb.Environment.Location foo = getPosition().getAdjacent(
					dirList[i]);
			if (foo == null)
				continue;
			// if (foo.equals(getPosition())) continue;
			if (foo != null && foo.getAgent() == null
					&& !foo.testFlag(ComplexEnvironment.FLAG_STONE)
					&& !foo.testFlag(ComplexEnvironment.FLAG_WASTE)
					&& !foo.testFlag(ComplexEnvironment.FLAG_FOOD)) {
				foo.setFlag(ComplexEnvironment.FLAG_FOOD, false);
				foo.setFlag(ComplexEnvironment.FLAG_STONE, false);
				foo.setFlag(ComplexEnvironment.FLAG_WASTE, true);
				ComplexEnvironment.addWaste(currTick, foo.v[0], foo.v[1],
						wasteInit, wasteRate);
				wasteAdded = true;
				i = dirList.length + 100;
				break;
			}
		}
		/*
		 * Crowded! IF there is no empty tile in which to drop the waste, we can
		 * replace a food tile with a waste tile... /* This function is assumed
		 * to add a waste tile! That is, this function assumes an existence of
		 * at least one food tile that it will be able to replace with a waste
		 * tile. Nothing happens otherwise.
		 */
		if (!wasteAdded) {
			for (int i = 0; i < dirList.length; i++) {
				cobweb.Environment.Location foo = getPosition().getAdjacent(
						dirList[i]);
				if (foo == null)
					continue;
				// if (foo.equals(getPosition())) continue;
				if (foo != null &&
				/* Hack: don't put a waste tile on top of an agent */
				foo.getAgent() == null &&
				/* Nuke a food pile */
				foo.testFlag(ComplexEnvironment.FLAG_FOOD)) {
					foo.setFlag(ComplexEnvironment.FLAG_FOOD, false);
					foo.setFlag(ComplexEnvironment.FLAG_WASTE, true);
					ComplexEnvironment.addWaste(currTick, foo.v[0], foo.v[1],
							wasteInit, wasteRate);
					wasteAdded = true;
					i = dirList.length + 100;
					break;
				}
			}
		}
	}

	public void eat(cobweb.Environment.Location destPos) {
		// Eat first before we can produce waste, of course.
		destPos.setFlag(ComplexEnvironment.FLAG_FOOD, false);
		// Gain Energy according to the food type.
		if (ComplexEnvironment.getFoodType(destPos) == agentType) {
			energy += foodEnergy;
			_wasteGain -= foodEnergy;
			info.addFoodEnergy(agentType, foodEnergy);
		} else {
			energy += otherFoodEnergy;
			_wasteGain -= otherFoodEnergy;
			info.addOthers(agentType, otherFoodEnergy);
		}
	}

	private void eat(ComplexAgent adjacentAgent) {
		energy += adjacentAgent.energy;
		_wasteGain -= adjacentAgent.energy;
		info.addCannibalism(agentType, adjacentAgent.energy);
		adjacentAgent.die();
	}

	void communicate(ComplexAgent target) {
		target.commInbox = commOutbox;
	}

	void broadcastFood(cobweb.Environment.Location loc) { // []SK
		String message = "Food found (" + loc.v[0] + " , " + loc.v[1] + ")";
		new CommPacket(CommPacket.FOOD, id, message, energy,
				broadcastEnergyBased, broadcastFixedRange);
		// new CommPacket sent
		/*
		 * System.out.println(CommPacket.FOOD); System.out.println(id);
		 * System.out.println("Food found"+loc.v[0]+" , "+loc.v[1]);
		 * System.out.println(energy);
		 */

		energy -= broadcastEnergyCost; // Deduct broadcasting cost from energy

		/* debug */
		/*
		 * System.out.println("Packet Sent. List has: "); for (int i=0;i<
		 * ComplexEnvironment.currentPackets.size();i++)
		 * System.out.println(((CommPacket)ComplexEnvironment.currentPackets.get(i)).getPacketId()); /*
		 * debug
		 */
	}

	void broadcastCheating(cobweb.Environment.Location loc) { // []SK
		String message = "Cheater encountered (" + loc.v[0] + " , " + loc.v[1]
				+ ")";
		new CommPacket(CommPacket.CHEATER, id, message, energy,
				broadcastEnergyBased, broadcastFixedRange);
		// new CommPacket sent
		energy -= broadcastEnergyCost; // Deduct broadcasting cost from energy
	}

	boolean checkCredibility(long agentId) {
		// check if dispatcherId is in list
		// if (agentId != null) {
		for (int i = 0; i < photo_num; i++) {
			if (photo_memory[i] == agentId) {
				return false;
			}
		}
		// }
		return true;
	}

	int checkforBroadcasts() {
		CommManager commManager = new CommManager();
		CommPacket commPacket = null;
		for (int i = 0; i < ComplexEnvironment.currentPackets.size(); i++) {
			commPacket = ComplexEnvironment.currentPackets.get(i);
			if (commManager.packetInRange(commPacket.getRadius(), /* commPacket. */
					getPosition(), getPosition()/* loc */))
				return i;
		}
		return -1;
	}

	void receiveFoodBroadcast(CommPacket commPacket) {
		@SuppressWarnings("unused")
		String message = commPacket.getContent();
		int foodPos = 0;
		;
		try {
			foodPos = 3;// Integer.parseInt(message);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in receiveFoodBroadcast()");
		}
		int dist = getPosition().v[0]/* or v[1] */- foodPos;
		new lookPair(dist, ComplexEnvironment.FLAG_FOOD);
	}

	void receiveCheatingBroadcast(CommPacket commPacket) {
		@SuppressWarnings("unused")
		String message = commPacket.getContent();
		int cheaterId = 0;
		try {
			cheaterId = 3;// Integer.parseInt(message);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in receiveCheatingBroadcast()");
		}
		photo_memory[photo_num % memory_size] = cheaterId;
	}

	void receiveBroadcast() {

		@SuppressWarnings("unused")
		CommManager commManager = new CommManager();
		CommPacket commPacket = null;

		commPacket = ComplexEnvironment.currentPackets
				.get(checkforBroadcasts());
		// check if dispatcherId is in list
		if (checkCredibility(commPacket.getDispatcherId()))
			;

		int type = commPacket.getType();
		switch (type) {
			case CommPacket.FOOD:
				receiveFoodBroadcast(commPacket);
				break;
			case CommPacket.CHEATER:
				receiveCheatingBroadcast(commPacket);
				break;
			/**
			 * TODO Add new broadcast types
			 */
			default:
				System.out.println("Unrecognized broadcast type");
		}
	}

	void turnRight() {
		cobweb.Environment.Direction newFacing = new cobweb.Environment.Direction(
				2);
		newFacing.v[0] = -facing.v[1];
		newFacing.v[1] = facing.v[0];
		facing = newFacing;
		energy -= turnRightEnergy;
		energy -= energyPenalty(true);
		_wasteLoss -= turnRightEnergy;
		info.useTurning(agentType, turnRightEnergy);
		if (energy <= 0)
			die();
		if (!pregnant && asexFlag && energy >= breedEnergy
				&& asexualBreedChance != 0.0
				&& cobweb.globals.random.nextFloat() < asexualBreedChance) {
			pregPeriod = pregnancyPeriod;
			pregnant = true;
		}
		if (pregnant) {
			pregPeriod--;
		}
		info.addTurn();
	}

	void turnLeft() {
		cobweb.Environment.Direction newFacing = new cobweb.Environment.Direction(
				2);
		newFacing.v[0] = facing.v[1];
		newFacing.v[1] = -facing.v[0];
		facing = newFacing;
		energy -= turnLeftEnergy;
		energy -= energyPenalty(true);
		_wasteLoss -= turnLeftEnergy;
		;
		info.useTurning(agentType, turnLeftEnergy);
		if (energy <= 0)
			die();
		if (!pregnant && asexFlag && energy >= breedEnergy
				&& asexualBreedChance != 0.0
				&& cobweb.globals.random.nextFloat() < asexualBreedChance) {
			pregPeriod = pregnancyPeriod;
			pregnant = true;
		}
		if (pregnant) {
			pregPeriod--;
		}
		info.addTurn();
	}

	// Provide a random facing for the agent.
	private void InitFacing() {
		double f = cobweb.globals.random.nextFloat();
		if (f < 0.25)
			facing = cobweb.Environment.DIRECTION_NORTH;
		else if (f < 0.5)
			facing = cobweb.Environment.DIRECTION_SOUTH;
		else if (f < 0.75)
			facing = cobweb.Environment.DIRECTION_EAST;
		else
			facing = cobweb.Environment.DIRECTION_WEST;
	}

	public int getAgentType() {
		return agentType;
	}

	public int getIntFacing() {
		if (facing.equals(cobweb.Environment.DIRECTION_NORTH))
			return 0;
		if (facing.equals(cobweb.Environment.DIRECTION_EAST))
			return 1;
		if (facing.equals(cobweb.Environment.DIRECTION_SOUTH))
			return 2;
		if (facing.equals(cobweb.Environment.DIRECTION_WEST))
			return 3;
		return 0;
	}

	// Constructor with two parents
	ComplexAgent(cobweb.Environment.Location pos, ComplexAgent parent,
			ComplexAgent parent2, int strat) {
		super(pos,
				ControllerFactory.createFromParents(
						parent.getController(), parent2.getController(), parent.mutationRate));
		InitFacing();

		try {
			genetic_code = new GeneticCode(GeneticCode
					.createGeneticCodeMeiosis(parent.getGeneticSequence(),
							parent2.getGeneticSequence()));
			if (cobweb.globals.random.nextFloat() <= parent.mutationRate) { // parent.mutationRate?
				genetic_code = new GeneticCode(GeneticCode.mutate(genetic_code
						.getGeneticInfo(), cobweb.globals.random
						.nextInt(GeneticCode.NUM_BITS)));
			}
		} catch (GeneticCodeException e) {
			System.out.println(e.getMessage());
		}
		copyConstants(parent);
		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(
				agentType, parent.info, parent2.info, strat);

		// Determine the agent's genetic code.

		((ComplexEnvironment) pos.getEnvironment()).getColorizer().colorAgent(
				this);
		info.setGeneticCode(this.getGeneticSequence(), this.getGeneticCode()
				.getGeneticColour()); // Records the agent's
		// genetic code.
		GATracker.addAgent(this); // Add this agent to the GATracker's list of
									// agent (presumably all living agents)
	}

	// Constructor with a parent; standard asexual copy
	ComplexAgent(cobweb.Environment.Location pos, ComplexAgent parent, int strat) {
		super(pos,
				ControllerFactory.createFromParent(
						parent.getController(), parent.mutationRate));
		InitFacing();
		try {
			genetic_code = new GeneticCode(parent.getGeneticSequence());
			if (cobweb.globals.random.nextFloat() <= parent.mutationRate) { // parent.mutationRate?
				genetic_code = new GeneticCode(GeneticCode.mutate(genetic_code
						.getGeneticInfo(), cobweb.globals.random
						.nextInt(GeneticCode.NUM_BITS)));
			}
		} catch (GeneticCodeException e) {
			System.out.println(e.getMessage());
		}

		copyConstants(parent);
		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(
				agentType, parent.info, strat);

		// Determine the agent's genetic code.

		((ComplexEnvironment) pos.getEnvironment()).getColorizer().colorAgent(
				this);
		info.setGeneticCode(this.getGeneticSequence(), this.getGeneticCode()
				.getGeneticColour()); // Records the agent's
		// genetic code.
		GATracker.addAgent(this); // Add this agent to the GATracker's list of
									// agent (presumably all living agents)
	}

	public void copyConstants(ComplexAgent p) {
		setConstants(p.agentType, p.agentPDAction, p.memory_size, p.food_bias,
				default_energy[p.agentType], default_foodEnergy[p.agentType],
				default_otherFoodEnergy[p.agentType],
				default_breedEnergy[p.agentType],
				default_pregnancyPeriod[p.agentType],
				default_stepEnergy[p.agentType],
				default_stepRockEnergy[p.agentType],
				default_turnRightEnergy[p.agentType],
				default_turnLeftEnergy[p.agentType],
				default_mutationRate[p.agentType], p.colorful,
				default_commSimMin[p.agentType],
				default_stepAgentEnergy[p.agentType],
				default_sexualBreedChance[p.agentType],
				default_asexualBreedChance[p.agentType],
				default_breedSimMin[p.agentType],
				default_sexualPregnancyPeriod[p.agentType], p.agingMode,
				default_agingLimit[p.agentType],
				default_agingRate[p.agentType], p.wasteMode, p.wastePen,
				p.wasteGain, p.wasteLoss, p.wasteRate, p.wasteInit,
				p.pdTitForTat, default_pdCoopProb[p.agentType],
				p.broadcastMode, p.broadcastEnergyBased,
				default_broadcastFixedRange[p.agentType],
				default_broadcastEnergyMin[p.agentType],
				default_broadcastEnergyCost[p.agentType], p.plantstoeat,
				p.agentstoeat, p.agentPDStrategy, // 1 == cheater, else
													// cooperator
				p.agentPDAction, // The agent's action; $$$$$ this parameter
									// seems useless, there is another PDaction
									// parameter above already. Apr 18
				p.lastPDMove, // Remember the opponent's move in the last game
				p.getGeneticSequence()); // The genetic sequence of agent.
	}

	// Constructor with no parent agent; creates an agent using "immaculate
	// conception" technique
	ComplexAgent(int agentT, cobweb.Environment.Location pos, int action,
			int memory, boolean foodBstrat, int initE, int foodE, int otherFE,
			int breedE, int pregP, int stepE, int stepRockE, int rightE,
			int leftE, float mutationR, boolean colors, int memoryB, int cSM,
			int stepA, float sChance, float aChance, float breedSM,
			int sexualPregP, int commB, boolean agingMode, int agingLimit,
			float agingRate, boolean wasteMode, int wastePen, int wasteGain,
			int wasteLoss, float wasteRate, int wasteInit, boolean pdTitForTat,
			int pdCoopProb, boolean broadcastMode,
			boolean broadcastEnergyBased, int broadcastFixedRange,
			int broadcastEnergyMin, int broadcastEnergyCost, int plants2eat[],
			int agents2eat[], int agentPDStrategy, // Tit-for-tat or
													// probability based
			int agentPDAction, // The agent's action; 1 = cheater, else
								// cooperator
			// $$$$$ this parameter seems useless, there is another PDaction
			// parameter above already. Apr 18
			int lastPDMove, // Remember the opponent's move in the last game
			String genetic_sequence) { // The genetic sequence of agent.

		super(pos, ControllerFactory.createNew(memoryB, commB));

		InitFacing();
		try {
			genetic_code = new GeneticCode(genetic_sequence); // Initialize
																// the
			// genetic code.
		} catch (GeneticCodeException e) {
			System.out.println(e.getMessage());
		}
		setConstants(agentT, action, memory, foodBstrat, initE, foodE, otherFE,
				breedE, pregP, stepE, stepRockE, rightE, leftE, mutationR,
				colors, cSM, stepA, sChance, aChance, breedSM, sexualPregP,
				agingMode, agingLimit, agingRate, wasteMode, wastePen,
				wasteGain, wasteLoss, wasteRate, wasteInit, pdTitForTat,
				pdCoopProb, broadcastMode, broadcastEnergyBased,
				broadcastFixedRange, broadcastEnergyMin, broadcastEnergyCost,
				plants2eat, agents2eat, agentPDStrategy, // Tit-for-tat or
															// probability based
				agentPDAction, // The agent's action; 1 = cheater, else
				// cooperator $$$$$ this parameter seems useless, there is
				// another PDaction parameter above already. Apr 18
				lastPDMove,// Remember the opponent's move in the last game
				genetic_sequence); // The genetic sequence of agent.

		// assign an ID
		// id = rand.nextLong();
		int[] agent_rgb_colour_values = this.getGeneticCode()
				.getGeneticColour();
		Color agent_colour = new Color(
				agent_rgb_colour_values[GeneticCode.RED_PIXEL_COLUMN],
				agent_rgb_colour_values[GeneticCode.GREEN_PIXEL_COLUMN],
				agent_rgb_colour_values[GeneticCode.BLUE_PIXEL_COLUMN]);
		this.setColor(agent_colour);
		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(
				agentType, action);
		info.setGeneticCode(this.getGeneticSequence(), this.getGeneticCode()
				.getGeneticColour()); // Records the agent's
		// genetic code.
		GATracker.addAgent(this); // Add this agent to the GATracker's list of
									// agent (presumably all living agents)

	}

	public void setConstants(int agentT, int action, int memory,
			boolean foodBstrat, int initE, int foodE, int otherFE, int breedE,
			int pregP, int stepE, int stepRockE, int rightE, int leftE,
			float mutationR, boolean colors, int cSM, int stepA, float sChance,
			float aChance, float breedSM, int sexualPregP, boolean aMode,
			int aLimit, float aRate, boolean wMode, int wPen, int wGain,
			int wLoss, float wRate, int wInit, boolean pdTat, int pdProb,
			boolean bMode, boolean bEnergy, int bRange, int bMin, int bCost,
			int plants2eat[], int agents2eat[], int agentPDStrategy, // Tit-for-tat
																		// or
																		// probability
																		// based
			// $$$$$ a redundant parameter for now (keep it for future using),
			// as we already have boolean "pdTitForTat". Apr 18
			int agentPDAction, // The agent's action; 1 =
			// cheater, else cooperator $$$$$ this parameter seems useless,
			// there is another PDaction parameter above already. Apr 18
			int lastPDMove, // Remember the opponent's move in
			// the last game
			String genetic_sequence) { // The genetic sequence
		// of agent.

		agentType = agentT;
		this.agentPDAction = action; // $$$$$$ change from "agentPDAction ==
										// action;", for agentPDAction was
										// duplicated to the parameter with the
										// same name. Apr 18
		memory_size = memory;
		photo_memory = new int[memory_size];
		food_bias = foodBstrat;
		energy = initE;
		foodEnergy = foodE;
		otherFoodEnergy = otherFE;
		breedEnergy = breedE;
		pregnancyPeriod = pregP;
		initEnergy = initE;
		stepEnergy = stepE;
		stepRockEnergy = stepRockE;
		turnLeftEnergy = leftE;
		turnRightEnergy = rightE;
		mutationRate = mutationR;
		colorful = colors;
		commSimMin = cSM;
		stepAgentEnergy = stepA;
		sexualBreedChance = sChance;
		asexualBreedChance = aChance;
		breedSimMin = breedSM;
		sexualPregnancyPeriod = sexualPregP;
		agingMode = aMode;
		agingLimit = aLimit;
		agingRate = aRate;
		wasteMode = wMode;
		wastePen = wPen;
		wasteGain = wGain;
		wasteLoss = wLoss;
		wasteRate = wRate;
		wasteInit = wInit;
		pdTitForTat = pdTat;
		pdCoopProb = pdProb;
		broadcastMode = bMode;
		broadcastEnergyBased = bEnergy;
		broadcastFixedRange = bRange;
		broadcastEnergyMin = bMin;
		broadcastEnergyCost = bCost;
		plantstoeat = plants2eat;
		agentstoeat = agents2eat;
		_wasteGain = wasteGain;
		_wasteLoss = wasteLoss;

		// $$$$$$ Modified the following block. we are not talking about RESET
		// here. Apr 18
		/*
		 * agentPDStrategy = 1; // Tit-for-tat or probability based
		 * agentPDAction = -1; // The agent's action; 1 = cheater, else
		 * cooperator $$$$$ this parameter seems useless, there is another
		 * PDaction parameter above already. Apr 18 lastPDMove = -1; // Remember
		 * the opponent's move in the last game
		 */
		this.agentPDStrategy = agentPDStrategy; // above all, sometimes a new
												// agent need copy PDStrategy
												// from its parent. See
												// copyConstants( ComplexAgent p
												// )
		this.lastPDMove = lastPDMove; // "KeepOldAgents" need pass this
										// parameter. (as a reasonable side
										// effect, the parameter of a parent
										// would also pass to its child)
		// See ComplexEnvironment.load(cobweb.Scheduler s, Parser p/*
		// java.io.Reader r */) @ if (keepOldAgents[0]) {...

		/** Initialize the mutable parameters into a TreeMap. */
		Map<String, Object> mutables = new TreeMap<String, Object>();
		mutables.put("Mutation Rate", mutationRate);
		mutables.put("Initial Energy", energy);
		mutables.put("Favourite Food Energy", foodEnergy);
		mutables.put("Other Food Energy", otherFoodEnergy);
		mutables.put("Breed Energy", breedEnergy);
		mutables.put("Pregnancy Period - 1 parent", pregnancyPeriod);
		mutables.put("Step Energy Loss", stepEnergy);
		mutables.put("Step Rock Energy Loss", stepRockEnergy);
		mutables.put("Turn Right Energy Loss", turnRightEnergy);
		mutables.put("Turn Left Energy Loss", turnLeftEnergy);
		mutables.put("Min. Communication Similarity", commSimMin);
		mutables.put("Step Agent Energy Loss", stepAgentEnergy);
		mutables.put("Pregnancy Period - 2 parents", sexualPregnancyPeriod);
		mutables.put("Min. Breed Similarity", breedSimMin);
		mutables.put("2 parents Breed Chance", sexualBreedChance);
		mutables.put("1 parent Breed Chance", asexualBreedChance);
		mutables.put("Aging Limit", agingLimit);
		mutables.put("Aging Rate", agingRate);
		mutables.put("PD Cooperation Probability", pdCoopProb);
		mutables.put("Broadcast fixed range", broadcastFixedRange);
		mutables.put("Broadcast Minimum Energy", broadcastEnergyMin);
		mutables.put("Broadcast Energy Cost", broadcastEnergyCost);
		Iterator<String> it = PhenotypeMaster.genetic_linkages.keySet()
				.iterator();
		String s = "";
		while (it.hasNext()) {
			if ((s = it.next()).equals("None")) {
				continue;
			}
			// Get gene position
			int gene_position = PhenotypeMaster.genetic_linkages.get(s);

			// Get the gene's value (0-255)
			int gene_value = this.getGeneticCode().getGeneticColour()[gene_position];

			// Get the appropriate coefficient associated with the gene value
			// Coefficient = absolute value of 2*sin(x), x being attribute value
			// in degrees
			double coefficient = 2 * Math.abs(Math.sin(gene_value * Math.PI
					/ 180));

			// Get instance variable linked to attribute in agent
			Object o = mutables.get(s);

			// Modify the value according to the coefficient.
			if (o instanceof Float) {
				mutables.put(s, new Float(((Float) o) * coefficient));
			} else if (o instanceof Integer) {
				/*
				 * This is really supposed to round a number and turn it into an
				 * int. I can't seem to find a neat way to do this :/
				 */
				mutables.put(s, (new Long(Math.round(((Integer) o)
						* coefficient))).intValue());
			}
		}

		/** Equate values stored in the TreeMap to the parameters. */
		mutationRate = (Float) mutables.get("Mutation Rate");
		energy = (Integer) mutables.get("Initial Energy");
		foodEnergy = (Integer) mutables.get("Favourite Food Energy");
		otherFoodEnergy = (Integer) mutables.get("Other Food Energy");
		breedEnergy = (Integer) mutables.get("Breed Energy");
		pregnancyPeriod = (Integer) mutables.get("Pregnancy Period - 1 parent");
		stepEnergy = (Integer) mutables.get("Step Energy Loss");
		stepRockEnergy = (Integer) mutables.get("Step Rock Energy Loss");
		turnRightEnergy = (Integer) mutables.get("Turn Right Energy Loss");
		turnLeftEnergy = (Integer) mutables.get("Turn Left Energy Loss");
		commSimMin = (Integer) mutables.get("Min. Communication Similarity");
		stepAgentEnergy = (Integer) mutables.get("Step Agent Energy Loss");
		sexualPregnancyPeriod = (Integer) mutables
				.get("Pregnancy Period - 2 parents");
		breedSimMin = (Float) mutables.get("Min. Breed Similarity");
		sexualBreedChance = (Float) mutables.get("2 parents Breed Chance");
		asexualBreedChance = (Float) mutables.get("1 parent Breed Chance");
		agingLimit = (Integer) mutables.get("Aging Limit");
		agingRate = (Float) mutables.get("Aging Rate");
		pdCoopProb = (Integer) mutables.get("PD Cooperation Probability");
		broadcastFixedRange = (Integer) mutables.get("Broadcast fixed range");
		broadcastEnergyMin = (Integer) mutables.get("Broadcast Minimum Energy");
		broadcastEnergyCost = (Integer) mutables.get("Broadcast Energy Cost");
	}

	@Override
	public int getAgentPDAction() {
		return agentPDAction;
	}

	@Override
	public int getAgentPDStrategy() {
		return agentPDStrategy;
	}

	private cobweb.Environment.Direction facing = cobweb.Environment.DIRECTION_NORTH;
}
