package cwcore;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cobweb.ColorLookup;
import cobweb.Point2D;
import cobweb.TypeColorEnumeration;
import cobweb.Environment.Location;
import cwcore.ComplexEnvironment.CommManager;
import cwcore.ComplexEnvironment.CommPacket;
import cwcore.complexParams.AgentMutator;
import cwcore.complexParams.ComplexAgentParams;
import cwcore.complexParams.ContactMutator;
import cwcore.complexParams.SpawnMutator;
import cwcore.complexParams.StepMutator;
import driver.ControllerFactory;

public class ComplexAgent extends cobweb.Agent implements cobweb.TickScheduler.Client {

	static class lookPair {
		private int dist;

		private int type;

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
	}

	/** Default mutable parameters of each agent type. */

	private static ComplexAgentParams defaulParams[];

	private static AgentSimularityCalculator simCalc;

	/** Sets the default mutable parameters of each agent type. */
	public static void setDefaultMutableParams(ComplexAgentParams[] params) {
		defaulParams = params.clone();
		for (int i = 0; i < params.length; i++) {
			defaulParams[i] = (ComplexAgentParams) params[i].clone();
		}
	}

	public static void setSimularityCalc(AgentSimularityCalculator calc) {
		simCalc = calc;
	}

	private int agentType;

	public ComplexAgentParams params;

	/* energy gauge */
	private int energy;

	/* Prisoner's Dilemma */
	private int agentPDStrategy; // tit-for-tat or probability

	private int pdCheater; // The agent's action; 1 == cheater, else

	// cooperator
	private int lastPDMove; // Remember the opponent's move in the last game

	private int commInbox;
	private int commOutbox;

	// memory size is the maximum capacity of the number of cheaters an agent
	// can remember
	private long photo_memory[];
	private int photo_num = 0;
	private boolean want2meet = false;
	boolean cooperate;

	private long birthTick = 0;

	private long age = 0;
	/* Waste variables */
	private int wasteCounterGain;
	private int wasteCounterLoss;
	private int memoryBuffer;
	private ComplexAgent breedPartner;
	private Color color = Color.lightGray;
	private boolean asexFlag;

	private ComplexAgentInfo info;
	// pregnancyPeriod is set value while pregPeriod constantly changes
	private int pregPeriod;

	private boolean pregnant = false;

	private static boolean tracked = false;

	// static-izing the writer. Doesn't seem feasible to have a writer for each
	// agent.
	// main issue is speed and also the need to find a graceful way to produce
	// the output
	// (dumping 200 .txt files != graceful).
	private static java.io.Writer writer;

	@SuppressWarnings("unused")
	private static long groupLastPolled = 1;

	public static final int LOOK_DISTANCE = 4;

	public static void clearData() {
		if (tracked)
			ComplexAgentInfo.resetGroupData();
	}

	public static void dumpData(long tick) {
		if (tracked)
			ComplexAgentInfo.dumpGroupData(tick, writer);
	}

	public static void setPrintWriter(java.io.Writer w) {
		writer = w;
	}

	// static-izing this too. see the comment above
	public static void tracked() {
		tracked = true;
	}

	/** The current tick we are in (or the last tick this agent was notified */
	private long currTick = 0;

	private static ColorLookup colorMap = TypeColorEnumeration.getInstance();

	private static Set<ContactMutator> contactMutators = new LinkedHashSet<ContactMutator>();

	private static Set<StepMutator> stepMutators = new LinkedHashSet<StepMutator>();

	private static final cobweb.Direction[] dirList = { cobweb.Environment.DIRECTION_NORTH,
			cobweb.Environment.DIRECTION_SOUTH, cobweb.Environment.DIRECTION_WEST, cobweb.Environment.DIRECTION_EAST,
			cobweb.Environment.DIRECTION_NORTHEAST, cobweb.Environment.DIRECTION_SOUTHEAST,
			cobweb.Environment.DIRECTION_NORTHWEST, cobweb.Environment.DIRECTION_SOUTHWEST };

	public static void addMutator(AgentMutator mutator) {
		if (mutator instanceof SpawnMutator)
			spawnMutators.add((SpawnMutator) mutator);

		if (mutator instanceof ContactMutator)
			contactMutators.add((ContactMutator) mutator);

		if (mutator instanceof StepMutator)
			stepMutators.add((StepMutator) mutator);
	}

	public static void clearMutators() {
		spawnMutators.clear();
		contactMutators.clear();
	}

	private cobweb.Direction facing = cobweb.Environment.DIRECTION_NORTH;

	private static Set<SpawnMutator> spawnMutators = new LinkedHashSet<SpawnMutator>();

	/**
	 * Constructor with two parents
	 *
	 * @param pos spawn position
	 * @param parent1 first parent
	 * @param parent2 second parent
	 * @param strat PD strategy
	 */
	ComplexAgent(cobweb.Environment.Location pos, ComplexAgent parent1, ComplexAgent parent2, int strat) {
		super(pos, ControllerFactory.createFromParents(parent1.getController(), parent2.getController(),
				parent1.params.mutationRate));
		InitFacing();

		copyConstants(parent1);
		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(agentType, parent1.info, parent2.info, strat);

		for (SpawnMutator mutator : spawnMutators)
			mutator.onSpawn(this, parent1, parent2);
	}

	/**
	 * Constructor with a parent; standard asexual copy
	 *
	 * @param pos spawn position
	 * @param parent parent
	 * @param strat PD strategy
	 */
	ComplexAgent(cobweb.Environment.Location pos, ComplexAgent parent, int strat) {
		super(pos, ControllerFactory.createFromParent(parent.getController(), parent.params.mutationRate));
		InitFacing();

		copyConstants(parent);
		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(agentType, parent.info, strat);

		for (SpawnMutator mutator : spawnMutators)
			mutator.onSpawn(this, parent);
	}

	/**
	 * Constructor with no parent agent; creates an agent using "immaculate conception" technique
	 *
	 * @param agentType agent type
	 * @param pos spawn position
	 * @param doCheat start PD off cheating?
	 * @param agentData agent parameters
	 */
	public ComplexAgent(int agentType, Location pos, int doCheat, ComplexAgentParams agentData) {
		super(pos, ControllerFactory.createNew(agentData.memoryBits, agentData.communicationBits));

		InitFacing();

		setConstants(doCheat, agentData);

		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(agentType, doCheat);

		for (SpawnMutator mutator : spawnMutators)
			mutator.onSpawn(this);
	}

	private void afterTurnAction() {
		energy -= energyPenalty(true);
		if (energy <= 0)
			die();
		if (!pregnant)
			tryAsexBreed();
		if (pregnant) {
			pregPeriod--;
		}
	}

	@Override
	public long birthday() {
		return birthTick;
	}

	void broadcastCheating(cobweb.Environment.Location loc) { // []SK
		// String message = "Cheater encountered (" + loc.v[0] + " , " + loc.v[1] + ")";
		String message = Long.toString(((ComplexAgent) loc.getAgent()).id);
		new CommPacket(CommPacket.CHEATER, id, message, energy, params.broadcastEnergyBased, params.broadcastFixedRange);
		// new CommPacket sent
		energy -= params.broadcastEnergyCost; // Deduct broadcasting cost from energy
	}

	void broadcastFood(cobweb.Environment.Location loc) { // []SK
		String message = loc.toString();
		new CommPacket(CommPacket.FOOD, id, message, energy, params.broadcastEnergyBased, params.broadcastFixedRange);
		// new CommPacket sent
		energy -= params.broadcastEnergyCost; // Deduct broadcasting cost from energy
	}

	private boolean canBroadcast() {
		return energy > params.broadcastEnergyMin;
	}

	public boolean canEat(cobweb.Environment.Location destPos) {
		return params.foodweb.canEatFood[ComplexEnvironment.getFoodType(destPos)];
	}

	private boolean canEat(ComplexAgent adjacentAgent) {
		boolean caneat = false;
		caneat = params.foodweb.canEatAgent[adjacentAgent.getAgentType()];
		if (this.energy > params.breedEnergy)
			caneat = false;

		return caneat;
	}

	boolean mustFlip = false;

	boolean canStep(Location destPos) {
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

	boolean checkCredibility(long agentId) {
		// check if dispatcherId is in list
		// if (agentId != null) {
		for (int i = 0; i < params.pdMemory; i++) {
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
			if (commManager.packetInRange(commPacket.getRadius(), getPosition(), getPosition()))
				return i;
		}
		return -1;
	}

	void communicate(ComplexAgent target) {
		target.commInbox = commOutbox;
	}

	public void copyConstants(ComplexAgent p) {
		setConstants(p.pdCheater, (ComplexAgentParams) defaulParams[p.getAgentType()].clone());
	}

	@Override
	public void die() {
		super.die();

		for (SpawnMutator mutator : spawnMutators) {
			mutator.onDeath(this);
		}

		info.setDeath(((ComplexEnvironment) position.getEnvironment()).getTickCount());
	}

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

	public void eat(cobweb.Environment.Location destPos) {
		// Eat first before we can produce waste, of course.
		destPos.setFlag(ComplexEnvironment.FLAG_FOOD, false);
		// Gain Energy according to the food type.
		if (ComplexEnvironment.getFoodType(destPos) == agentType) {
			energy += params.foodEnergy;
			wasteCounterGain -= params.foodEnergy;
			info.addFoodEnergy(agentType, params.foodEnergy);
		} else {
			energy += params.otherFoodEnergy;
			wasteCounterGain -= params.otherFoodEnergy;
			info.addOthers(agentType, params.otherFoodEnergy);
		}
	}

	private void eat(ComplexAgent adjacentAgent) {
		int gain = (int) (adjacentAgent.energy * params.agentFoodEnergy);
		energy += gain;
		wasteCounterGain -= gain;
		info.addCannibalism(agentType, gain);
		adjacentAgent.die();
	}

	private double energyPenalty(boolean log) {
		if (!params.agingMode)
			return 0.0;
		double age = currTick - birthTick;
		if (tracked && log)
			info.useExtraEnergy(agentType, Math.min(Math.max(0, energy), (int) (params.agingRate
					* (Math.tan(((age / params.agingLimit) * 89.99) * Math.PI / 180)) + 0.5)));

		return Math.min(Math.max(0, energy), params.agingRate
				* (Math.tan(((age / params.agingLimit) * 89.99) * Math.PI / 180)));
	}

	cobweb.Agent getAdjacentAgent() {
		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
		if (destPos == null) {
			return null;
		}
		return destPos.getAgent();
	}

	public long getAge() {
		return age;
	}

	@Override
	public int getAgentPDAction() {
		return pdCheater;
	}

	@Override
	public int getAgentPDStrategy() {
		return agentPDStrategy;
	}

	public int getAgentType() {
		return params.type;
	}

	@Override
	public Color getColor() {
		return color;
	}

	public int getCommInbox() {
		return commInbox;
	}

	public int getCommOutbox() {
		return commOutbox;
	}

	// get agent's drawing information given the UI
	@Override
	public void getDrawInfo(cobweb.UIInterface theUI) {
		Color stratColor;

		// is agents action is 1, it's a cheater therefore it's
		// graphical representation will a have red boundary
		if (pdCheater == 1) {
			stratColor = Color.red;
		} else {
			// cooperator, black boundary
			stratColor = Color.black;
		}
		// based on agent type
		theUI.newAgent(getColor(), colorMap.getColor(agentType, 1), stratColor, new Point2D(getPosition().v[0],
				getPosition().v[1]), new Point2D(facing.v[0], facing.v[1]));
	}

	// return agent's energy
	@Override
	public int getEnergy() {
		return energy;
	}

	public ComplexAgentInfo getInfo() {
		return info;
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

	public int getMemoryBuffer() {
		return memoryBuffer;
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

	public boolean isAsexFlag() {
		return asexFlag;
	}

	private void iveBeenCheated(int othersID) {

		if (params.pdMemory > 0) {
			photo_memory[photo_num++] = othersID;

			if (photo_num >= params.pdMemory) {
				photo_num = 0;
			}
		}

		broadcastCheating(getPosition());
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

	/**
	 * This method determines the action of the agent in a PD game
	 */
	public void playPD() {

		double coopProb = params.pdCoopProb / 100.0d; // static value for now

		if (params.pdTitForTat) { // if true then agent is playing TitForTat
			agentPDStrategy = 1; // set Strategy to 1 i.e. TitForTat
			if (lastPDMove == -1)// if this is the first move
				lastPDMove = 0; // start by cooperating
			// might include probability bias within TitForTat strategy...not
			// currently implemented
			pdCheater = lastPDMove;
		} else {
			agentPDStrategy = 0; // $$$$$$ added to ensure Strategy is set to
			// 0, i.e. probability. Apr 22
			pdCheater = 0; // agent is assumed to cooperate
			float rnd = cobweb.globals.random.nextFloat();
			// System.out.println("rnd: " + rnd);
			// System.out.println("coopProb: " + coopProb);
			if (rnd > coopProb)
				pdCheater = 1; // agent defects depending on
			// probability
		}

		return;
	}

	private void playPDonStep(ComplexAgent adjacentAgent, int othersID) {
		playPD();
		adjacentAgent.playPD();

		lastPDMove = adjacentAgent.pdCheater; // Adjacent Agent's action is assigned to the last move memory of the
		// agent
		adjacentAgent.lastPDMove = pdCheater; // Agent's action is assigned to the last move memory of the adjacent
		// agent

		/*
		 * TODO: The ability for the PD game to contend for the Get the food tiles immediately around each agents
		 */

		/* 0 = cooperate. 1 = defect */

		/*
		 * Payoff Matrix: 0 0 => 5 5 0 1 => 2 8 1 0 => 8 2 1 1 => 3 3
		 */

		@SuppressWarnings("unused")
		final int PD_STATIC_PAYOFF = 0;

		final int PD_COOPERATE = 0;
		final int PD_DEFECT = 1;

		if (pdCheater == PD_COOPERATE && adjacentAgent.pdCheater == PD_COOPERATE) {
			/* REWARD */
			energy += ComplexEnvironment.PD_PAYOFF_REWARD;
			adjacentAgent.energy += ComplexEnvironment.PD_PAYOFF_REWARD;

		} else if (pdCheater == PD_COOPERATE && adjacentAgent.pdCheater == PD_DEFECT) {
			/* SUCKER */
			energy += ComplexEnvironment.PD_PAYOFF_SUCKER;
			adjacentAgent.energy += ComplexEnvironment.PD_PAYOFF_TEMPTATION;

			iveBeenCheated(othersID);

		} else if (pdCheater == PD_DEFECT && adjacentAgent.pdCheater == PD_COOPERATE) {
			/* TEMPTATION */
			energy += ComplexEnvironment.PD_PAYOFF_TEMPTATION;
			adjacentAgent.energy += ComplexEnvironment.PD_PAYOFF_SUCKER;

		} else if (pdCheater == PD_DEFECT && adjacentAgent.pdCheater == PD_DEFECT) {
			/* PUNISHMENT */
			energy += ComplexEnvironment.PD_PAYOFF_PUNISHMENT;
			adjacentAgent.energy += ComplexEnvironment.PD_PAYOFF_PUNISHMENT; // $$$$$$

			iveBeenCheated(othersID);
		}

	}

	/*
	 * Record the state here for logging. Can also be treated as a "setup" function before doing any activities. Might
	 * be useful in the future. This is only run when tracking is enabled.
	 */
	private void poll() {
		info.addEnergy(agentType, energy);
		info.alive(agentType);
	}

	void receiveBroadcast() {

		@SuppressWarnings("unused")
		CommManager commManager = new CommManager();
		CommPacket commPacket = null;

		commPacket = ComplexEnvironment.currentPackets.get(checkforBroadcasts());
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
			default:
				System.out.println("Unrecognized broadcast type");
		}
	}

	void receiveCheatingBroadcast(CommPacket commPacket) {
		String message = commPacket.getContent();
		long cheaterId = 0;
		cheaterId = Long.parseLong(message);
		photo_memory[photo_num] = cheaterId;
	}

	void receiveFoodBroadcast(CommPacket commPacket) {
		String message = commPacket.getContent();
		String[] xy = message.split(",");
		@SuppressWarnings("unused")
		int x = Integer.parseInt(xy[0]);
		@SuppressWarnings("unused")
		int y = Integer.parseInt(xy[1]);
	}

	public void setAsexFlag(boolean asexFlag) {
		this.asexFlag = asexFlag;
	}

	@Override
	public void setColor(Color c) {
		color = c;
	}

	public void setCommInbox(int commInbox) {
		this.commInbox = commInbox;
	}

	public void setCommOutbox(int commOutbox) {
		this.commOutbox = commOutbox;
	}

	public void setConstants(int pdCheat, ComplexAgentParams agentData) {

		this.params = agentData;
		this.agentType = agentData.type;
		this.pdCheater = pdCheat;
		energy = agentData.initEnergy;
		wasteCounterGain = params.wasteLimitGain;
		wasteCounterLoss = params.wasteLimitLoss;

		photo_memory = new long[params.pdMemory];

		// $$$$$$ Modified the following block. we are not talking about RESET
		// here. Apr 18
		/*
		 * agentPDStrategy = 1; // Tit-for-tat or probability based agentPDAction = -1; // The agent's action; 1 =
		 * cheater, else cooperator $$$$$ this parameter seems useless, there is another PDaction parameter above
		 * already. Apr 18 lastPDMove = -1; // Remember the opponent's move in the last game
		 */
		this.agentPDStrategy = 0; // FIXME agentData.agentPDStrategy; // above all, sometimes a new
		// agent need copy PDStrategy
		// from its parent. See
		// copyConstants( ComplexAgent p
		// )
		this.lastPDMove = 0; // FIXME agentData.lastPDMove; // "KeepOldAgents" need pass this
		// parameter. (as a reasonable side
		// effect, the parameter of a parent
		// would also pass to its child)
		// See ComplexEnvironment.load(cobweb.Scheduler s, Parser p/*
		// java.io.Reader r */) @ if (keepOldAgents[0]) {...

	}

	public void setMemoryBuffer(int memoryBuffer) {
		this.memoryBuffer = memoryBuffer;
	}

	/*
	 * return the measure of similiarity between this agent and the 'other' ranging from 0.0 to 1.0 (identical)
	 */
	@Override
	public double similarity(cobweb.Agent other) {
		if (!(other instanceof ComplexAgent))
			return 0.0;
		return // ((GeneticController) controller)
		// .similarity((GeneticController) ((ComplexAgent) other)
		// .getController());
		((LinearWeightsController) controller).similarity((LinearWeightsController) other.getController());
	}

	@Override
	public double similarity(int other) {
		return 0.5; // ((GeneticController) controller).similarity(other);
	}

	public void step() {
		cobweb.Agent adjAgent;
		mustFlip = getPosition().checkFlip(facing);
		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);

		if (canStep(destPos)) {

			// Check for food...
			cobweb.Environment.Location breedPos = null;
			if (destPos.testFlag(ComplexEnvironment.FLAG_FOOD)) {
				if (params.broadcastMode & canBroadcast()) {
					broadcastFood(destPos);
				}
				if (canEat(destPos)) {
					eat(destPos);
				}
				if (pregnant && energy >= params.breedEnergy && pregPeriod <= 0) {

					breedPos = getPosition();
					energy -= params.initEnergy;
					energy -= energyPenalty(true);
					wasteCounterLoss -= params.initEnergy;
					info.useOthers(agentType, params.initEnergy);

				} else {
					if (!pregnant)
						tryAsexBreed();
				}
			}

			for (StepMutator m : stepMutators)
				m.onStep(this, getPosition(), destPos);

			move(destPos);

			if (breedPos != null) {

				if (breedPartner == null) {
					info.addDirectChild();
					new ComplexAgent(breedPos, this, this.pdCheater);
				} else {
					// child's strategy is determined by its parents, it has a
					// 50% chance to get either parent's strategy
					int childStrategy = -1;
					if (this.pdCheater != -1) {
						boolean choose = cobweb.globals.random.nextBoolean();
						if (choose) {
							childStrategy = this.pdCheater;
						} else {
							childStrategy = breedPartner.pdCheater;
						}
					}

					info.addDirectChild();
					breedPartner.info.addDirectChild();
					new ComplexAgent(breedPos, this, breedPartner, childStrategy);
					info.addSexPreg();
				}
				breedPartner = null;
				pregnant = false;
			}
			energy -= params.stepEnergy;
			wasteCounterLoss -= params.stepEnergy;
			info.useStepEnergy(agentType, params.stepEnergy);
			info.addStep();
			info.addPathStep(this.getPosition());

		} else if ((adjAgent = getAdjacentAgent()) != null && adjAgent instanceof ComplexAgent
				&& ((ComplexAgent) adjAgent).info != null) {
			// two agents meet

			ComplexAgent adjacentAgent = (ComplexAgent) adjAgent;


			for (ContactMutator mut : contactMutators) {
				mut.onContact(this, adjacentAgent);
			}

			if (canEat(adjacentAgent)) {
				eat(adjacentAgent);
			}

			if (this.pdCheater != -1) {// $$$$$ if playing Prisoner's
				// Dilemma. Please refer to ComplexEnvironment.load, "// spawn new random agents for each type"
				want2meet = true;
			}

			int othersID = adjacentAgent.info.getAgentNumber();
			// scan the memory array, is the 'other' agents ID is found in the array,
			// then choose not to have a transaction with him.
			for (int i = 0; i < params.pdMemory; i++) {
				if (photo_memory[i] == othersID) {
					want2meet = false;
				}
			}
			// if the agents are of the same type, check if they have enough
			// resources to breed
			if (adjacentAgent.agentType == agentType) {

				double sim = 0.0;
				boolean canBreed = !pregnant && energy >= params.breedEnergy && params.sexualBreedChance != 0.0
						&& cobweb.globals.random.nextFloat() < params.sexualBreedChance;

				// Generate genetic similarity number
				sim = simCalc.similarity(this, adjacentAgent);

				if (sim >= params.commSimMin) {
					communicate(adjacentAgent);
				}

				if (canBreed && sim >= params.breedSimMin
						&& ((want2meet && adjacentAgent.want2meet) || (pdCheater == -1))) {
					pregnant = true;
					pregPeriod = params.sexualPregnancyPeriod;
					breedPartner = adjacentAgent;
				}
			}
			// perform the transaction only if non-pregnant and both agents want to meet
			if (!pregnant && want2meet && adjacentAgent.want2meet) {

				playPDonStep(adjacentAgent, othersID);
			}
			energy -= params.stepAgentEnergy;
			wasteCounterLoss -= params.stepAgentEnergy;
			info.useAgentBumpEnergy(agentType, params.stepAgentEnergy);
			info.addAgentBump();

		} // end of two agents meet
		else if (destPos != null && destPos.testFlag(ComplexEnvironment.FLAG_WASTE)) {
			// Bumps into waste
			energy -= params.wastePen;
			wasteCounterLoss -= params.wastePen;
			info.useRockBumpEnergy(agentType, params.wastePen);
			info.addRockBump();
		} else {
			// Rock bump
			energy -= params.stepRockEnergy;
			wasteCounterLoss -= params.stepRockEnergy;
			info.useRockBumpEnergy(agentType, params.stepRockEnergy);
			info.addRockBump();
		}
		energy -= energyPenalty(true);

		if (energy <= 0)
			die();

		if (energy < params.breedEnergy) {
			pregnant = false;
			breedPartner = null;
		}

		if (pregnant) {
			pregPeriod--;
		}
	}

	public void tickNotification(long tick) {
		if (!isAlive())
			return;

		/* The current tick */
		currTick = tick;

		/* Hack to find the birth tick... */
		if (birthTick == 0)
			birthTick = currTick;

		age++;

		/* Time to die, Agent Bond */
		if (params.agingMode) {
			if ((currTick - birthTick) >= params.agingLimit) {
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
		if (params.wasteMode)
			tryPoop();

		/* Check if broadcasting is enabled */
		if (params.broadcastMode & !ComplexEnvironment.currentPackets.isEmpty())
			receiveBroadcast();// []SK
	}

	private void tryAsexBreed() {
		if (asexFlag && energy >= params.breedEnergy && params.asexualBreedChance != 0.0
				&& cobweb.globals.random.nextFloat() < params.asexualBreedChance) {
			pregPeriod = params.asexPregnancyPeriod;
			pregnant = true;
		}
	}

	/**
	 * Produce waste
	 */
	private void tryPoop() {
		// we should check before calling this function instead
		// if (!wasteMode) return;
		boolean produce = false;
		if (wasteCounterGain <= 0) {
			produce = true;
			wasteCounterGain += params.wasteLimitGain;
		} else if (wasteCounterLoss <= 0) {
			produce = true;
			wasteCounterLoss += params.wasteLimitLoss;
		}
		if (!produce)
			return;

		boolean wasteAdded = false;
		/* Output a waste somewhere "close" (rad 1 from currentPosition) */
		for (int i = 0; i < dirList.length; i++) {
			cobweb.Environment.Location foo = getPosition().getAdjacent(dirList[i]);
			if (foo == null)
				continue;
			// if (foo.equals(getPosition())) continue;
			if (foo != null && foo.getAgent() == null && !foo.testFlag(ComplexEnvironment.FLAG_STONE)
					&& !foo.testFlag(ComplexEnvironment.FLAG_WASTE) && !foo.testFlag(ComplexEnvironment.FLAG_FOOD)) {
				foo.setFlag(ComplexEnvironment.FLAG_FOOD, false);
				foo.setFlag(ComplexEnvironment.FLAG_STONE, false);
				foo.setFlag(ComplexEnvironment.FLAG_WASTE, true);
				ComplexEnvironment.addWaste(currTick, foo.v[0], foo.v[1], params.wasteInit, params.wasteDecay);
				wasteAdded = true;
				i = dirList.length + 100;
				break;
			}
		}
		/*
		 * Crowded! IF there is no empty tile in which to drop the waste, we can replace a food tile with a waste
		 * tile... / This function is assumed to add a waste tile! That is, this function assumes an existence of at
		 * least one food tile that it will be able to replace with a waste tile. Nothing happens otherwise.
		 */
		if (!wasteAdded) {
			for (int i = 0; i < dirList.length; i++) {
				cobweb.Environment.Location foo = getPosition().getAdjacent(dirList[i]);
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
					ComplexEnvironment.addWaste(currTick, foo.v[0], foo.v[1], params.wasteInit, params.wasteDecay);
					wasteAdded = true;
					i = dirList.length + 100;
					break;
				}
			}
		}
	}

	public void turnLeft() {
		cobweb.Direction newFacing = new cobweb.Direction(2);
		newFacing.v[0] = facing.v[1];
		newFacing.v[1] = -facing.v[0];
		facing = newFacing;
		energy -= params.turnLeftEnergy;
		wasteCounterLoss -= params.turnLeftEnergy;
		info.useTurning(agentType, params.turnLeftEnergy);
		info.addTurn();
		afterTurnAction();
	}

	public void turnRight() {
		cobweb.Direction newFacing = new cobweb.Direction(2);
		newFacing.v[0] = -facing.v[1];
		newFacing.v[1] = facing.v[0];
		facing = newFacing;
		energy -= params.turnRightEnergy;
		wasteCounterLoss -= params.turnRightEnergy;
		info.useTurning(agentType, params.turnRightEnergy);
		info.addTurn();
		afterTurnAction();
	}

	@Override
	public int type() {
		return agentType;
	}

	@Override
	public void tickZero() {

	}

	public static Iterable<String> logDataTotal() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logDataTotal())
				blah.add(s);
		}
		return blah;
	}

	public static Iterable<String> logHederTotal() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logHeaderTotal())
				blah.add(s);
		}
		return blah;
	}

	public static Collection<String> logHederAgent() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logHeadersAgent())
				blah.add(s);
		}
		return blah;
	}

	public static Collection<String> logDataAgent(int i) {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logDataAgent(i))
				blah.add(s);
		}
		return blah;
	}

	@Override
	public void move(Location newPos) {
		super.move(newPos);
		if (mustFlip) {
			if (facing.equals(ComplexEnvironment.DIRECTION_NORTH))
				facing = ComplexEnvironment.DIRECTION_SOUTH;
			else if (facing.equals(ComplexEnvironment.DIRECTION_SOUTH))
				facing = ComplexEnvironment.DIRECTION_NORTH;
			else if (facing.equals(ComplexEnvironment.DIRECTION_EAST))
				facing = ComplexEnvironment.DIRECTION_WEST;
			else if (facing.equals(ComplexEnvironment.DIRECTION_WEST))
				facing = ComplexEnvironment.DIRECTION_EAST;
		}
	}
}
