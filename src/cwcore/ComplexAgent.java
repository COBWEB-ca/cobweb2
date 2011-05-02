package cwcore;

// Food storage
// Vaccination/avoid infected agents
// Wordbuilding

// Make learning toggleable
// React to temperatures

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import learning.LearningAgentParams;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cobweb.ColorLookup;
import cobweb.Direction;
import cobweb.DrawingHandler;
import cobweb.Environment;
import cobweb.Environment.Location;
import cobweb.Point2D;
import cobweb.TypeColorEnumeration;
import cobweb.globals;
import cwcore.ComplexEnvironment.CommManager;
import cwcore.ComplexEnvironment.CommPacket;
import cwcore.complexParams.AgentMutator;
import cwcore.complexParams.ComplexAgentParams;
import cwcore.complexParams.ContactMutator;
import cwcore.complexParams.SpawnMutator;
import cwcore.complexParams.StepMutator;
import driver.ControllerFactory;

public class ComplexAgent extends cobweb.Agent implements cobweb.TickScheduler.Client, Serializable {

	static class SeeInfo {

		private int dist;

		private int type;

		public SeeInfo(int d, int t) {
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

	/**
	 * 
	 */
	private static final long serialVersionUID = -5310096345506441368L;

	/** Default mutable parameters of each agent type. */
	private static ComplexAgentParams defaulParams[];

	private static LearningAgentParams learningParams[];

	private static AgentSimilarityCalculator simCalc;

	private static ArrayList<Occurrence> allOccurrences = new ArrayList<Occurrence>();

	private Collection<MemorableEvent> memEvents;

	private Collection<Queueable> queueables;

	/*
	 * MemorableEvents are placed in the agent's memory with this method. Earliest memories will
	 * be forgotten when the memory limit is exceeded.
	 * 
	 * TODO: Forget memories as time passes
	 */
	public void remember(MemorableEvent event) {
		if (event == null) {
			return;
		}
		if (memEvents == null) {
			memEvents = new ArrayList<MemorableEvent>();
		}
		memEvents.add(event);
		if (memEvents.size() > lParams.numMemories) {
			memEvents.remove(0);
		}
	}

	/*
	 * Events are queued using this method
	 */
	public void queue(Queueable act) {
		if (act == null) {
			return;
		}
		if (queueables == null) {
			queueables = new ArrayList<Queueable>();
		}
		queueables.add(act);
	}

	public static Collection<String> logDataAgent(int i) {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logDataAgent(i))
				blah.add(s);
		}
		return blah;
	}

	public static Iterable<String> logDataTotal() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logDataTotal())
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

	public static Iterable<String> logHederTotal() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logHeaderTotal())
				blah.add(s);
		}
		return blah;
	}

	/** Sets the default mutable parameters of each agent type. */
	public static void setDefaultMutableParams(ComplexAgentParams[] params, LearningAgentParams[] lParams) {
		defaulParams = params.clone();
		for (int i = 0; i < params.length; i++) {
			defaulParams[i] = (ComplexAgentParams) params[i].clone();
		}

		learningParams = lParams.clone();
		for (int i = 0; i < params.length; i++) {
			learningParams[i] = (LearningAgentParams) lParams[i].clone();
		}		
	}

	public static void setSimularityCalc(AgentSimilarityCalculator calc) {
		simCalc = calc;
	}

	private int agentType = 0;

	public ComplexAgentParams params;

	public LearningAgentParams lParams;

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

	// $$$$$$ Changed March 21st, breedPos used to be local to the step() method
	private cobweb.Environment.Location breedPos = null;

	private static boolean tracked = false;

	// static-izing the writer. Doesn't seem feasible to have a writer for each
	// agent.
	// main issue is speed and also the need to find a graceful way to produce
	// the output
	// (dumping 200 .txt files != graceful).
	private static java.io.Writer writer;

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

	public long getCurrTick() {
		return currTick;
	}

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
		stepMutators.clear();
	}

	private cobweb.Direction facing = cobweb.Environment.DIRECTION_NORTH;

	private static Set<SpawnMutator> spawnMutators = new LinkedHashSet<SpawnMutator>();

	boolean mustFlip = false;

	/**
	 * Constructor with two parents
	 * 
	 * @param pos spawn position
	 * @param parent1 first parent
	 * @param parent2 second parent
	 * @param strat PD strategy
	 */
	protected ComplexAgent(cobweb.Environment.Location pos, ComplexAgent parent1, ComplexAgent parent2, int strat) {
		super(ControllerFactory.createFromParents(parent1.getController(), parent2.getController(),
				parent1.params.mutationRate));
		InitFacing();

		if (globals.random.nextBoolean()) {
			lParams = parent1.lParams;
		} else {
			lParams = parent2.lParams;
		}

		copyConstants(parent1);

		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(agentType, parent1.info, parent2.info, strat);

		move(pos);

		pos.getEnvironment().getScheduler().addSchedulerClient(this);

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
	protected ComplexAgent(cobweb.Environment.Location pos, ComplexAgent parent, int strat) {
		super(ControllerFactory.createFromParent(parent.getController(), parent.params.mutationRate));
		InitFacing();

		lParams = parent.lParams;

		copyConstants(parent);
		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(agentType, parent.info, strat);

		move(pos);

		pos.getEnvironment().getScheduler().addSchedulerClient(this);

		for (SpawnMutator mutator : spawnMutators)
			mutator.onSpawn(this, parent);
	}

	/**   */
	public ComplexAgent(int agentT, int doCheat, ComplexAgentParams agentData, Direction facingDirection, Location pos) {
		super(ControllerFactory.createNew(agentData.memoryBits, agentData.communicationBits));

		setConstants(doCheat, agentData);
		this.facing = facingDirection;

		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(agentT, doCheat);

		move(pos);

		pos.getEnvironment().getScheduler().addSchedulerClient(this);

		for (SpawnMutator mutator : spawnMutators)
			mutator.onSpawn(this);
	}

	/**
	 * Constructor with no parent agent; creates an agent using
	 * "immaculate conception" technique
	 * 
	 * @param agentType agent type
	 * @param pos spawn position
	 * @param doCheat start PD off cheating?
	 * @param agentData agent parameters
	 */
	public ComplexAgent(int agentType, Location pos, int doCheat, ComplexAgentParams agentData, LearningAgentParams lAgentData) {
		super(ControllerFactory.createNew(agentData.memoryBits, agentData.communicationBits));
		setConstants(doCheat, agentData);

		InitFacing();

		params = agentData;
		lParams = lAgentData;

		info = ((ComplexEnvironment) (pos.getEnvironment())).addAgentInfo(agentType, doCheat);
		this.agentType = agentType;

		move(pos);

		pos.getEnvironment().getScheduler().addSchedulerClient(this);

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

	public void changeEnergy(int amount) {
		energy += amount;
	}

	void broadcastCheating(cobweb.Environment.Location loc) { // []SK
		// String message = "Cheater encountered (" + loc.v[0] + " , " +
		// loc.v[1] + ")";
		String message = Long.toString(((ComplexAgent) loc.getAgent()).id);
		new CommPacket(CommPacket.CHEATER, id, message, energy, params.broadcastEnergyBased, params.broadcastFixedRange);
		// new CommPacket sent
		energy -= params.broadcastEnergyCost; // Deduct broadcasting cost from
		// energy
	}

	void broadcastFood(cobweb.Environment.Location loc) { // []SK
		String message = loc.toString();
		new CommPacket(CommPacket.FOOD, id, message, energy, params.broadcastEnergyBased, params.broadcastFixedRange);
		// new CommPacket sent

		// Deduct broadcasting cost from energy
		if ((energy -= params.broadcastEnergyCost) > 0) {
			// If still alive, the agent remembers that it is displeasing to
			// lose energy
			// due to broadcasting
			float howSadThisMakesMe = Math.max(Math.min((float) -params.broadcastEnergyCost / (float) energy, 1), -1);
			remember(new MemorableEvent(currTick, howSadThisMakesMe, "broadcast"));
		}
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

	// @Override
	// public Object clone() {
	// ComplexAgent cp = new ComplexAgent(getAgentType(), pdCheater, params,
	// facing);
	// //cp.hibernate();
	// return cp;
	// }

	void communicate(ComplexAgent target) {
		target.setCommInbox(commOutbox);
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

	public SeeInfo distanceLook() {
		Direction d = facing;
		cobweb.Environment.Location destPos = getPosition().getAdjacent(d);
		if (getPosition().checkFlip(d))
			d = d.flip();
		for (int dist = 1; dist <= LOOK_DISTANCE; ++dist) {

			// We are looking at the wall
			if (destPos == null)
				return new SeeInfo(dist, ComplexEnvironment.FLAG_STONE);

			// Check for stone...
			if (destPos.testFlag(ComplexEnvironment.FLAG_STONE))
				return new SeeInfo(dist, ComplexEnvironment.FLAG_STONE);

			// If there's another agent there, then return that it's a stone...
			if (destPos.getAgent() != null && destPos.getAgent() != this)
				return new SeeInfo(dist, ComplexEnvironment.FLAG_AGENT);

			// If there's food there, return the food...
			if (destPos.testFlag(ComplexEnvironment.FLAG_FOOD))
				return new SeeInfo(dist, ComplexEnvironment.FLAG_FOOD);

			if (destPos.testFlag(ComplexEnvironment.FLAG_WASTE))
				return new SeeInfo(dist, ComplexEnvironment.FLAG_WASTE);

			destPos = destPos.getAdjacent(d);
			if (getPosition().checkFlip(d))
				d = d.flip();
		}
		return new SeeInfo(LOOK_DISTANCE, 0);
	}

	public void eat(cobweb.Environment.Location destPos) {
		// Eat first before we can produce waste, of course.
		destPos.setFlag(ComplexEnvironment.FLAG_FOOD, false);
		// Gain Energy according to the food type.
		if (ComplexEnvironment.getFoodType(destPos) == agentType) {
			energy += params.foodEnergy;
			wasteCounterGain -= params.foodEnergy;
			info.addFoodEnergy(params.foodEnergy);

			// Eating food is ideal!!
			remember(new MemorableEvent(currTick, lParams.foodPleasure, "food"));
		} else {
			energy += params.otherFoodEnergy;
			wasteCounterGain -= params.otherFoodEnergy;
			info.addOthers(params.otherFoodEnergy);

			// Eating other food has a ratio of goodness compared to eating
			// normal food.
			float howHappyThisMakesMe = (float) params.otherFoodEnergy / (float) params.foodEnergy
			* lParams.foodPleasure;
			remember(new MemorableEvent(currTick, howHappyThisMakesMe, "food"));
		}
	}

	private void eat(ComplexAgent adjacentAgent) {
		int gain = (int) (adjacentAgent.energy * params.agentFoodEnergy);
		energy += gain;
		wasteCounterGain -= gain;
		info.addCannibalism(gain);
		adjacentAgent.die();

		// Bloodily consuming agents makes us happy
		remember(new MemorableEvent(currTick, lParams.ateAgentPleasure, "ateAgent"));
	}

	double energyPenalty(boolean log) {
		if (!params.agingMode)
			return 0.0;
		double tempAge = currTick - birthTick;
		assert (tempAge == age);
		int penaltyValue = Math.min(Math.max(0, energy),
				(int) (params.agingRate * (Math.tan(((tempAge / params.agingLimit) * 89.99) * Math.PI / 180))));
		if (tracked && log) {
			info.useExtraEnergy(penaltyValue);
		}

		return penaltyValue;
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
	public void getDrawInfo(DrawingHandler theUI) {
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

		remember(new MemorableEvent(this.age, -1, "agent-" + othersID));

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

	public Node makeNode(Document doc) {

		Node agent = doc.createElement("Agent");

		Element agentTypeElement = doc.createElement("agentType");
		agentTypeElement.appendChild(doc.createTextNode(agentType + ""));
		agent.appendChild(agentTypeElement);

		Element doCheatElement = doc.createElement("doCheat");
		doCheatElement.appendChild(doc.createTextNode(pdCheater + ""));
		agent.appendChild(doCheatElement);

		Element paramsElement = doc.createElement("params");

		params.saveConfig(paramsElement, doc);

		agent.appendChild(paramsElement);

		Element directionElement = doc.createElement("direction");

		facing.saveAsANode(directionElement, doc);

		agent.appendChild(directionElement);

		return agent;
	}

	@Override
	public void move(Location newPos) {
		super.move(newPos);
		info.addPathStep(newPos);
		if (mustFlip) {
			if (facing.equals(Environment.DIRECTION_NORTH))
				facing = Environment.DIRECTION_SOUTH;
			else if (facing.equals(Environment.DIRECTION_SOUTH))
				facing = Environment.DIRECTION_NORTH;
			else if (facing.equals(Environment.DIRECTION_EAST))
				facing = Environment.DIRECTION_WEST;
			else if (facing.equals(Environment.DIRECTION_WEST))
				facing = Environment.DIRECTION_EAST;
		}
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
			if (rnd > coopProb)
				pdCheater = 1; // agent defects depending on
			// probability
		}

		info.setStrategy(pdCheater);

		return;
	}

	private void playPDonStep(ComplexAgent adjacentAgent, int othersID) {
		playPD();
		adjacentAgent.playPD();

		lastPDMove = adjacentAgent.pdCheater; // Adjacent Agent's action is
		// assigned to the last move
		// memory of the
		// agent
		adjacentAgent.lastPDMove = pdCheater; // Agent's action is assigned to
		// the last move memory of the
		// adjacent
		// agent

		/*
		 * TODO LOW: The ability for the PD game to contend for the Get the food tiles immediately around each agents
		 */

		/* 0 = cooperate. 1 = defect */

		/*
		 * Payoff Matrix: 0 0 => 5 5, 0 1 => 2 8, 1 0 => 8 2, 1 1 => 3 3
		 */

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
	 * Record the state here for logging. Can also be treated as a "setup"
	 * function before doing any activities. Might be useful in the future. This
	 * is only run when tracking is enabled.
	 */
	private void poll() {
		info.addEnergy(energy);
		info.alive();
	}

	void receiveBroadcast() {
		// TODO: Add a MemorableEvent to show a degree of friendliness towards
		// the broadcaster

		CommPacket commPacket = null;

		commPacket = ComplexEnvironment.currentPackets.get(checkforBroadcasts());
		// check if dispatcherId is in list
		// TODO what does this do?
		checkCredibility(commPacket.getDispatcherId());

		int type = commPacket.getType();
		switch (type) {
			case CommPacket.FOOD:
				receiveFoodBroadcast(commPacket);
				break;
			case CommPacket.CHEATER:
				receiveCheatingBroadcast(commPacket);
				break;
			default:
				Logger myLogger = Logger.getLogger("COBWEB2");
				myLogger.log(Level.WARNING, "Unrecognised broadcast type");
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
		String[] xy = message.substring(1, message.length() - 1).split(",");
		int x = Integer.parseInt(xy[0]);
		int y = Integer.parseInt(xy[1]);
		thinkAboutFoodLocation(x, y);

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
		setWasteCounterLoss(params.wasteLimitLoss);

		photo_memory = new long[params.pdMemory];

		// $$$$$$ Modified the following block. we are not talking about RESET
		// here. Apr 18
		/*
		 * agentPDStrategy = 1; // Tit-for-tat or probability based
		 * agentPDAction = -1; // The agent's action; 1 = cheater, else
		 * cooperator $$$$$ this parameter seems useless, there is another
		 * PDaction parameter above already. Apr 18 lastPDMove = -1; // Remember
		 * the opponent's move in the last game
		 */
		this.agentPDStrategy = 0; // FIXME agentData.agentPDStrategy;
		// above all, sometimes a new
		// agent need copy PDStrategy
		// from its parent. See
		// copyConstants( ComplexAgent p
		// )
		this.lastPDMove = 0; // FIXME agentData.lastPDMove;
		// "KeepOldAgents" need pass this
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
	 * return the measure of similiarity between this agent and the 'other'
	 * ranging from 0.0 to 1.0 (identical)
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
		final cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);

		if (canStep(destPos)) {

			// Check for food...
			if (destPos.testFlag(ComplexEnvironment.FLAG_FOOD)) {

				// Queues the agent to broadcast about the food
				queue(new SmartAction(this, "broadcast") {

					@Override
					public void desiredAction(ComplexAgent agent) {
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
						public void desiredAction(ComplexAgent agent) {
							agent.eat(destPos);
						}

					});
				}

				if (pregnant && energy >= params.breedEnergy && pregPeriod <= 0) {

					queue(new BreedInitiationOccurrence(this, 0, "breedInit", breedPartner.id));

				} else {
					if (!pregnant) {
						// Manages asexual breeding
						queue(new SmartAction(this, "asexBreed") {

							@Override
							public void desiredAction(ComplexAgent agent) {
								agent.tryAsexBreed();
							}

						});
					}
				}
			}

			queue(new Occurrence(this, 0, "stepMutate") {
				@Override
				MemorableEvent effect(ComplexAgent concernedAgent) {
					for (StepMutator m : stepMutators)
						m.onStep(ComplexAgent.this, getPosition(), destPos);
					return null;
				}
			});

			// Move the agent to destPos
			queue(new SmartAction(this, "move-" + destPos.toString()) {

				@Override
				public void desiredAction(ComplexAgent agent) {
					agent.move(destPos);
				}
			});

			// Try to breed
			queue(new Occurrence(this, 0, "breed") {

				@Override
				MemorableEvent effect(ComplexAgent concernedAgent) {
					if (concernedAgent.getBreedPos() != null) {

						if (concernedAgent.breedPartner == null) {
							concernedAgent.getInfo().addDirectChild();
							ComplexAgent child = new ComplexAgent(concernedAgent.getBreedPos(), concernedAgent,
									concernedAgent.pdCheater);

							// Retain emotions for our child!
							concernedAgent.remember(new MemorableEvent(currTick, lParams.emotionForChildren, "agent-" + child.id));
						} else {
							// child's strategy is determined by its parents, it
							// has a
							// 50% chance to get either parent's strategy

							// We like the agent we are breeding with; remember
							// that
							// this agent is favourable
							concernedAgent.remember(new MemorableEvent(currTick, lParams.loveForPartner, "agent-" + breedPartner.id));

							int childStrategy = -1;
							if (concernedAgent.pdCheater != -1) {
								boolean choose = cobweb.globals.random.nextBoolean();
								if (choose) {
									childStrategy = concernedAgent.pdCheater;
								} else {
									childStrategy = concernedAgent.breedPartner.pdCheater;
								}
							}

							concernedAgent.getInfo().addDirectChild();
							concernedAgent.breedPartner.getInfo().addDirectChild();
							ComplexAgent child = new ComplexAgent(concernedAgent.getBreedPos(), concernedAgent,
									concernedAgent.breedPartner, childStrategy);

							// Retain an undying feeling of love for our
							// child
							MemorableEvent weLoveOurChild = new MemorableEvent(currTick, lParams.emotionForChildren, "" + child);
							concernedAgent.remember(weLoveOurChild);
							concernedAgent.breedPartner.remember(weLoveOurChild);

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
				public MemorableEvent effect(ComplexAgent concernedAgent) {
					MemorableEvent ret = super.effect(concernedAgent);

					concernedAgent.setWasteCounterLoss(getWasteCounterLoss() - concernedAgent.params.stepEnergy);
					concernedAgent.getInfo().useStepEnergy(params.stepEnergy);
					concernedAgent.getInfo().addStep();
					concernedAgent.getInfo().addPathStep(concernedAgent.getPosition());

					return ret;
				}
			});

		} else if ((adjAgent = getAdjacentAgent()) != null && adjAgent instanceof ComplexAgent
				&& ((ComplexAgent) adjAgent).info != null) {
			// two agents meet

			final ComplexAgent adjacentAgent = (ComplexAgent) adjAgent;

			queue(new Occurrence(this, 0, "contactMutate") {

				@Override
				public MemorableEvent effect(ComplexAgent concernedAgent) {
					for (ContactMutator mut : contactMutators) {
						mut.onContact(concernedAgent, adjacentAgent);
					}
					return null;
				}
			});

			if (canEat(adjacentAgent)) {
				//An action to conditionally eat the agent
				queue(new SmartAction(this, "agent-" + adjacentAgent.id) {

					@Override
					public void desiredAction(ComplexAgent agent) {
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
						adjacentAgent.remember(new MemorableEvent(currTick, lParams.sparedEmotion, "agent-" + id));
					}
				});
			}

			// TODO: the logical structure for want2meet and photo_memory and such
			// can likely be replaced by using MemorableEvents
			if (this.pdCheater != -1) {// $$$$$ if playing Prisoner's
				// Dilemma. Please refer to ComplexEnvironment.load,
				// "// spawn new random agents for each type"
				want2meet = true;
			}

			final int othersID = adjacentAgent.info.getAgentNumber();
			// scan the memory array, is the 'other' agents ID is found in the
			// array,
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
					// Communicate with the smiliar agent
					queue(new SmartAction(this, "communicate") {

						@Override
						public void desiredAction(ComplexAgent agent) {
							agent.communicate(adjacentAgent);
						}
					});
				}

				if (canBreed && sim >= params.breedSimMin
						&& ((want2meet && adjacentAgent.want2meet) || (pdCheater == -1))) {
					// Initiate pregnancy
					queue(new SmartAction(this, "breed") {

						@Override
						public void desiredAction(ComplexAgent agent) {
							agent.pregnant = true;
							agent.pregPeriod = agent.params.sexualPregnancyPeriod;
							agent.breedPartner = adjacentAgent;
						}
					});

				}
			}
			// perform the transaction only if non-pregnant and both agents want
			// to meet
			if (!pregnant && want2meet && adjacentAgent.want2meet) {

				queue(new SmartAction(this) {

					@Override
					public void desiredAction(ComplexAgent agent) {
						agent.playPDonStep(adjacentAgent, othersID);
					}
				});

			}
			energy -= params.stepAgentEnergy;
			setWasteCounterLoss(getWasteCounterLoss() - params.stepAgentEnergy);
			info.useAgentBumpEnergy(params.stepAgentEnergy);
			info.addAgentBump();

		} // end of two agents meet
		else if (destPos != null && destPos.testFlag(ComplexEnvironment.FLAG_WASTE)) {

			// Allow agents up to a distance of 5 to see this agent hit the
			// waste
			queue(new Occurrence(this, 5, "bumpWaste") {

				@Override
				MemorableEvent effect(ComplexAgent concernedAgent) {
					concernedAgent.queue(new EnergyChangeOccurrence(concernedAgent, -params.wastePen, "bumpWaste"));
					setWasteCounterLoss(getWasteCounterLoss() - params.wastePen);
					info.useRockBumpEnergy(params.wastePen);
					info.addRockBump();
					return null;
				}
			});

		} else {
			// Rock bump
			queue(new Occurrence(this, 0, "bumpRock") {

				@Override
				MemorableEvent effect(ComplexAgent concernedAgent) {
					concernedAgent
					.queue(new EnergyChangeOccurrence(concernedAgent, -params.stepRockEnergy, "bumpRock"));
					setWasteCounterLoss(getWasteCounterLoss() - params.stepRockEnergy);
					info.useRockBumpEnergy(params.stepRockEnergy);
					info.addRockBump();
					return null;
				}
			});
		}

		// Energy penalty
		queue(new EnergyChangeOccurrence(this, -(int) energyPenalty(true), "energyPenalty"));

		if (energy <= 0)
			queue(new SmartAction(this) {

				@Override
				public void desiredAction(ComplexAgent agent) {
					agent.die();
				}
			});

		if (energy < params.breedEnergy) {
			queue(new SmartAction(this) {

				@Override
				public void desiredAction(ComplexAgent agent) {
					agent.pregnant = false;
					agent.breedPartner = null;
				}
			});
		}

		if (pregnant) {
			// Reduce pregnancy period
			queue(new Occurrence(this, 0, "preg") {

				@Override
				MemorableEvent effect(ComplexAgent concernedAgent) {
					concernedAgent.pregPeriod--;
					return null;
				}
			});
		}
	}

	private void thinkAboutFoodLocation(int x, int y) {
		Location target = this.getPosition().getEnvironment().getLocation(x, y);

		double closeness = 1;

		if (!target.equals(getPosition()))
			closeness = 1 / target.distance(this.getPosition());

		int o = (int) Math.round(closeness * (1 << this.params.communicationBits - 1));

		setCommInbox(o);
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

		ComplexEnvironment.Location loc = this.getPosition();

		ArrayList<Occurrence> rem = new ArrayList<Occurrence>();

		for (Occurrence oc : allOccurrences) {
			if (oc.time - currTick >= 0) {
				ComplexAgent occTarget = oc.target;
				ComplexEnvironment.Location loc2 = occTarget.getPosition();
				if (loc.distance(loc2) <= oc.detectableDistance
						&& (lParams.learnFromDifferentOthers || occTarget.type() == type())) {
					String desc = null;

					if (facing.equals(cobweb.Environment.DIRECTION_EAST)) {
						if (loc2.v[1] > loc.v[1]) {
							desc = "turnRight";
						} else if (loc2.v[1] != loc.v[1]) {
							desc = "turnLeft";
						}
					} else if (facing.equals(cobweb.Environment.DIRECTION_WEST)) {
						if (loc2.v[1] > loc.v[1]) {
							desc = "turnLeft";
						} else if (loc2.v[1] != loc.v[1]) {
							desc = "turnRight";
						}
					} else if (facing.equals(cobweb.Environment.DIRECTION_NORTH)) {
						if (loc2.v[0] > loc.v[0]) {
							desc = "turnRight";
						} else if (loc2.v[0] != loc.v[0]) {
							desc = "turnLeft";
						}
					} else if (facing.equals(cobweb.Environment.DIRECTION_SOUTH)) {
						if (loc2.v[0] > loc.v[0]) {
							desc = "turnLeft";
						} else if (loc2.v[0] != loc.v[0]) {
							desc = "turnRight";
						}
					}

					if (desc != null) {
						remember(new MemorableEvent(currTick, oc.event.magnitude, desc){
							//This information applies to only the present step the agent is about to take;
							//it will be irrelevant in the future (because new occurrences will be present)
							@Override
							public boolean forgetAfterStep() {
								return true;
							}
						});
					}
				}
			} else {
				rem.add(oc);
			}
		}

		allOccurrences.removeAll(rem);		
	}

	private void purgeMemory() {
		if (memEvents != null) {
			ArrayList<MemorableEvent> rem2 = new ArrayList<MemorableEvent>();
			for (MemorableEvent me : memEvents) {
				if (me.forgetAfterStep()) {
					rem2.add(me);
				}
			}
			memEvents.removeAll(rem2);
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

		observeOccurrences();

		/* Move/eat/reproduce/etc */
		controller.controlAgent(this);

		/*
		 * Perform all queued actions
		 */
		if (queueables != null && !queueables.isEmpty()) {
			// Use a second Collection to avoid concurrent modification issues
			ArrayList<Queueable> queueablesCopy = new ArrayList<Queueable>();
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


	public void tickZero() {
		// nothing
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
		boolean produce = false;
		if (wasteCounterGain <= 0 && params.wasteLimitGain > 0) {
			produce = true;
			wasteCounterGain += params.wasteLimitGain;
		} else if (getWasteCounterLoss() <= 0 && params.wasteLimitLoss > 0) {
			produce = true;
			setWasteCounterLoss(getWasteCounterLoss() + params.wasteLimitLoss);
		}
		if (!produce)
			return;

		boolean wasteAdded = false;
		/* Output a waste somewhere "close" (rad 1 from currentPosition) */
		for (int i = 0; i < dirList.length; i++) {
			cobweb.Environment.Location foo = getPosition().getAdjacent(dirList[i]);
			if (foo == null)
				continue;
			if (foo.getAgent() == null && !foo.testFlag(ComplexEnvironment.FLAG_STONE)
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
		 * Crowded! IF there is no empty tile in which to drop the waste, we can
		 * replace a food tile with a waste tile... / This function is assumed
		 * to add a waste tile! That is, this function assumes an existence of
		 * at least one food tile that it will be able to replace with a waste
		 * tile. Nothing happens otherwise.
		 */
		if (!wasteAdded) {
			for (int i = 0; i < dirList.length; i++) {
				cobweb.Environment.Location foo = getPosition().getAdjacent(dirList[i]);
				if (foo == null)
					continue;

				if (foo.getAgent() == null && foo.testFlag(ComplexEnvironment.FLAG_FOOD)) {
					/* Hack: don't put a waste tile on top of an agent */
					/* Nuke a food pile */
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
		setWasteCounterLoss(getWasteCounterLoss() - params.turnLeftEnergy);
		info.useTurning(params.turnLeftEnergy);
		info.addTurn();
		afterTurnAction();
	}


	public void turnRight() {
		cobweb.Direction newFacing = new cobweb.Direction(2);
		newFacing.v[0] = -facing.v[1];
		newFacing.v[1] = facing.v[0];
		facing = newFacing;
		energy -= params.turnRightEnergy;
		setWasteCounterLoss(getWasteCounterLoss() - params.turnRightEnergy);
		info.useTurning(params.turnRightEnergy);
		info.addTurn();
		afterTurnAction();
	}


	@Override
	public int type() {
		return agentType;
	}


	public void setBreedPos(cobweb.Environment.Location breedPos) {
		this.breedPos = breedPos;
	}


	public cobweb.Environment.Location getBreedPos() {
		return breedPos;
	}


	public void setWasteCounterLoss(int wasteCounterLoss) {
		this.wasteCounterLoss = wasteCounterLoss;
	}


	public int getWasteCounterLoss() {
		return wasteCounterLoss;
	}

	// ============QUEUEABLE============

	public static interface Queueable extends Describeable {

		public void happen();

		public boolean isComplete();
	}


	public static interface Describeable {

		public String getDescription();
	}

	// ============OCCURRENCE============
	public static abstract class Occurrence implements Queueable {

		float detectableDistance;
		ComplexAgent target;
		long time;
		MemorableEvent event;
		private boolean hasOccurred = false;
		String desc;

		public Occurrence(ComplexAgent target, float detectableDistance, String desc) {
			this.target = target;
			time = target.getCurrTick();
			this.detectableDistance = detectableDistance;
			this.desc = desc;
			allOccurrences.add(this);
		}

		// Effects the agent in whatever way necessary returning a memory of the
		// effect
		abstract MemorableEvent effect(ComplexAgent concernedAgent);

		// Causes the effect to occur, initializes the event, places the memory
		// in the agent's memory,
		// returns the event
		public final void happen() {
			event = effect(target);
			target.remember(event);
			hasOccurred = true;
		}

		public MemorableEvent getEvent() {
			if (event == null) {
				throw new NullPointerException("Must call occur() before calling getEvent()!");
			}
			return event;
		}

		public boolean hasOccurred() {
			return hasOccurred;
		}

		public boolean isComplete() {
			return true;
		}

		public final String getDescription() {
			return desc;
		}
	}

	// ============OCCURRENCE SUBCLASSES============

	public static class EnergyChangeOccurrence extends Occurrence {

		private int amountChanged;

		public EnergyChangeOccurrence(ComplexAgent target, int amountChanged, String desc) {
			this(target, amountChanged, desc, 0);
		}

		public EnergyChangeOccurrence(ComplexAgent target, float detectableDistance, String desc, int amountChanged) {
			super(target, detectableDistance, desc);
			this.amountChanged = amountChanged;
		}

		@Override
		public MemorableEvent effect(ComplexAgent concernedAgent) {
			int originalEnergy = concernedAgent.getEnergy();
			concernedAgent.changeEnergy(amountChanged);
			float magnitude = (float) amountChanged / (float) originalEnergy;
			return new EnergyMemEvent(time, magnitude, "energyChange");
		}

		public int getAmountChanged() {
			return amountChanged;
		}
	}

	public static class BreedInitiationOccurrence extends Occurrence {

		private long partnerID;

		public BreedInitiationOccurrence(ComplexAgent target, float detectableDistance, String desc, long partnerID) {
			super(target, detectableDistance, desc);
			this.partnerID = partnerID;
		}

		public long getPartnerID() {
			return partnerID;
		}

		@Override
		public MemorableEvent effect(ComplexAgent concernedAgent) {
			// Setting breedPos to non-null will cause the agent
			// to breed later
			concernedAgent.setBreedPos(concernedAgent.getPosition());

			// Subtract starting energy and energy penalty from
			// energy
			int energyLost = (int) (concernedAgent.params.initEnergy + concernedAgent.energyPenalty(true));

			EnergyChangeOccurrence energyChange = new EnergyChangeOccurrence(concernedAgent, 5f, "breed", -energyLost);
			energyChange.happen();

			concernedAgent.setWasteCounterLoss(concernedAgent.getWasteCounterLoss() - concernedAgent.params.initEnergy);

			concernedAgent.getInfo().useOthers(concernedAgent.params.initEnergy);

			return new BreedInitiationMemEvent(time, +0.5f, "breedInit");
		}

	}

	public static class BreedOccurrence extends Occurrence {

		public BreedOccurrence(ComplexAgent target, float detectableDistance, String desc) {
			super(target, detectableDistance, desc);
		}

		@Override
		public MemorableEvent effect(ComplexAgent concernedAgent) {

			return null;
		}
	}

	// ============SMART ACTION============

	public static abstract class SmartAction implements Queueable {

		String desc;
		boolean isIrrelevant = false;
		ComplexAgent agent;

		public SmartAction(ComplexAgent agent) {
			this(agent, "default");
		}

		public SmartAction(ComplexAgent agent, Object obj) {
			this(agent, obj.getClass().getName());
		}

		/**
		 * Create a new action with no information other than a description
		 * 
		 * @param desc a String to describe the action
		 */
		public SmartAction(ComplexAgent agent, String desc) {
			this.desc = desc;
			this.agent = agent;
		}

		public String getDescription() {
			return desc;
		}

		/**
		 * @return true if the action is no longer relevant (if it has already
		 *         been performed.)
		 */
		boolean irrelevantIfActionPerformed() {
			return true;
		}

		boolean irrelevantIfActionFailed() {
			return true;
		}

		/**
		 * @param event an event that may or may not be relevant
		 * @return true if the parameter event is relevant to this action
		 */
		public boolean eventIsRelated(MemorableEvent event) {
			return desc.equals(event.desc);
		}

		/*
		 * @return a list of all events related to this action
		 */
		final ArrayList<MemorableEvent> getRelatedEvents() {
			ArrayList<MemorableEvent> ret = new ArrayList<MemorableEvent>();
			if (getAgent().memEvents != null) {
				for (MemorableEvent me : getAgent().memEvents) {
					if (eventIsRelated(me)) {
						ret.add(me);
					}
				}
			}
			return ret;
		}

		/**
		 * @return whether or not this action ought to be performed By default
		 *         returns true if totalMagnitude >= 0
		 */
		boolean actionIsDesireable() {
			return totalMagnitude() >= 0;
		}

		/**
		 * @return the sum of the magnitudes of all related events
		 */
		final float totalMagnitude() {
			float ret = 0;

			for (MemorableEvent me : getRelatedEvents()) {
				ret += getMagnitudeFromEvent(me);
			}

			return ret;
		}

		public float getMagnitudeFromEvent(MemorableEvent event) {
			return event.getMagnitude();
		}

		/**
		 * The action that the agent is questioning whether or not to perform
		 */
		abstract void desiredAction(ComplexAgent agent);

		/**
		 * What to do if the wantedAction() is undesireable. By default, cancels
		 * the action from ever being performed (by stating isIrrelevant =
		 * false)
		 */
		void actionIfUndesireable() {
		}

		ComplexAgent getAgent() {
			return agent;
		}

		/**
		 * Performs desiredAction if it is a desireable thing to do. This is the
		 * method called by the agent.
		 * 
		 * desiredAction() will always be called if ignoreLearning = true.
		 */
		public final void happen() {

			if (!agent.lParams.shouldLearn || actionIsDesireable()) {
				desiredAction(getAgent());
				if (!agent.lParams.shouldLearn || irrelevantIfActionPerformed()) {
					isIrrelevant = true;
				}
			} else {
				actionIfUndesireable();
				if (irrelevantIfActionFailed()) {
					isIrrelevant = true;
				}
			}
		}

		public boolean isComplete() {
			return isIrrelevant;
		}
	}

	public static class MemorableEvent implements Describeable {

		private final long time;
		private final float magnitude;
		String desc;

		public MemorableEvent(long time, float magnitude) {
			this(time, magnitude, null);
		}

		public MemorableEvent(long time, float magnitude, String desc) {
			if (desc == null) {
				desc = getClass().getName();
			}
			this.time = time;
			this.magnitude = magnitude;
			this.desc = desc;
		}

		public long getTime() {
			return time;
		}

		public float getMagnitude() {
			return magnitude;
		}

		public String getDescription() {
			return desc;
		}

		public boolean isDesireable() {
			return magnitude > 0;
		}

		public boolean isDespicable() {
			return magnitude < 0;
		}

		public boolean forgetAfterStep() {
			return false;
		}
	}

	// ============MEMORABLE_EVENT SUBCLASSES============
	public static class EnergyMemEvent extends MemorableEvent {

		public EnergyMemEvent(long time, float magnitude, String desc) {
			super(time, magnitude, desc);
		}
	}

	public static class BreedInitiationMemEvent extends MemorableEvent {

		public BreedInitiationMemEvent(long time, float magnitude, String desc) {
			super(time, magnitude, desc);
		}
	}

	public static class BreedMemEvent extends MemorableEvent {

		public BreedMemEvent(long time, float magnitude, String desc) {
			super(time, magnitude, desc);
		}
	}

}
