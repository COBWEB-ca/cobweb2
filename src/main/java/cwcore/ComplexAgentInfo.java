/*
 * $$$$$: Comments by Liang $$$$$$: Codes modified and/or added by Liang
 */

package cwcore;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cobweb.Environment.Location;

public class ComplexAgentInfo {

	public static final int MAX_PATH_HISTORY = 32;

	/** The gene status of the agent. Default is 1. */

	static int agentTypes = 0; // total agent types

	static int[] deadAgentsofType; // numbers of agents dead of type i.

	// This is initialized in printAgent method.
	static int[] aliveAgentsofType;

	static int[] sexAgentsofType;

	static int[] asexAgentsofType;

	static int[] offspringsAgentsofType;

	static int[] stepsAgentsofType;

	static int[] totalsAgentsofType;

	static int[] stepslivedAgentsofType;

	static int[] eatenAgentsofType;

	/**
	 * This method should be called prior to calling the printInfo method. This method will initializes the static
	 * array. Should be called once not in a loop.
	 */

	public static void initStaticAgentInfo(int agenttypes) {
		if (alreadyInitialized) {
			Logger myLogger = Logger.getLogger("COBWEB2");
			myLogger.log(Level.WARNING, "ComplexAgentInfo::initStaticAgentInfo(" + agenttypes + ")"
					+ " ComplexAgentInfo::alreadyInitialized = " + alreadyInitialized);
			return;
		}
		alreadyInitialized = true;

		agentTypes = agenttypes;
		deadAgentsofType = new int[agentTypes];
		aliveAgentsofType = new int[agentTypes];
		sexAgentsofType = new int[agentTypes];
		asexAgentsofType = new int[agentTypes];
		offspringsAgentsofType = new int[agentTypes];
		stepsAgentsofType = new int[agentTypes];
		totalsAgentsofType = new int[agentTypes];
		stepslivedAgentsofType = new int[agentTypes];
		eatenAgentsofType = new int[agentTypes];
	}

	/* emulating C's printf() fcn */
	private static String paddMe(int i, int pad) {
		String tmp = i + "";
		return paddMe(tmp, pad);
	}

	private static String paddMe(long i, int pad) {
		String tmp = i + "";
		return paddMe(tmp, pad);
	}

	private static String paddMe(String s, int pad) {
		return paddMe(s, pad, ' ');
	}

	/* Allows custom padding. */
	private static String paddMe(String s, int pad, char cpad) {
		String ret = "";
		for (; pad >= s.length(); pad--) {
			ret += cpad;
		}
		ret += s;
		return ret;
	}

	public static void resetGroupData() {
		for (int i = 0; i < typesCount; i++) {
			energies[i] = 0;
			foodEnergies[i] = 0;
			stepEnergies[i] = 0;
			agentBumpEnergies[i] = 0;
			rockBumpEnergies[i] = 0;
			liveCount[i] = 0;
			otherEnergySources[i] = 0;
			cannibalEnergies[i] = 0;
			turningEnergies[i] = 0;
			otherEnergySinks[i] = 0;
			agentAgingEnergies[i] = 0;
		}
	}

	private boolean cheater = false;

	private long parent1 = -1;

	private long parent2 = -1;

	private long birthTick = -1;

	private long deathTick = -1;

	private int countSteps;

	private int countTurns;

	private List<Location> path = new LinkedList<Location>();

	private int countAgentBumps;

	private int countRockBumps;

	// The static variables to keep track of the agents produced so far.

	private int agentNumber;

	private int type;

	private int sexualPregs;

	private int directChildren;

	private static boolean alreadyInitialized = false;

	private static int typesCount = 4;

	private static int[] energies;

	private static int[] stepEnergies;

	private static int[] agentBumpEnergies;

	private static int[] rockBumpEnergies;

	private static int[] liveCount;

	private static int[] foodEnergies;

	private static int[] otherEnergySources;

	private static int[] cannibalEnergies;

	private static int[] turningEnergies;

	private static int[] otherEnergySinks;

	private static int[] agentAgingEnergies;

	public static void initialize(int agentCount) {
		typesCount = agentCount;

		energies = new int[typesCount];

		stepEnergies = new int[typesCount];

		agentBumpEnergies = new int[typesCount];

		rockBumpEnergies = new int[typesCount];

		liveCount = new int[typesCount];

		foodEnergies = new int[typesCount];

		otherEnergySources = new int[typesCount];

		cannibalEnergies = new int[typesCount];

		turningEnergies = new int[typesCount];

		otherEnergySinks = new int[typesCount];

		agentAgingEnergies = new int[typesCount];
	}

	public ComplexAgentInfo(int num, int type, long birth, ComplexAgentInfo p1, ComplexAgentInfo p2, boolean cheat) {
		this.type = type;
		agentNumber = num;
		birthTick = birth;
		parent1 = p1 != null ? p1.agentNumber : -1;
		parent2 = p2 != null ? p2.agentNumber : -1;
		cheater = cheat;
	}

	public ComplexAgentInfo(int num, int type, long birth, ComplexAgentInfo p1, boolean cheat) {
		this.type = type;
		agentNumber = num;
		birthTick = birth;
		parent1 = p1 != null ? p1.agentNumber : -1;
		cheater = cheat;
	}

	public ComplexAgentInfo(int num, int type, long birth, boolean cheat) {
		this.type = type;
		agentNumber = num;
		birthTick = birth;
		cheater = cheat;
	}

	public void addAgentBump() {
		++countAgentBumps;
	}

	/** Adds to the total energy gained from eating other agents. */
	public void addCannibalism(int val) {
		cannibalEnergies[type] += val;
	}

	public void addDirectChild() {
		++directChildren;
	}

	public void addFoodEnergy(int val) {
		foodEnergies[type] += val;
	}

	/** Other sources including energy gained from the agent strategy */
	public void addOthers(int val) {
		otherEnergySources[type] += val;
	}

	int boundNorth = Integer.MAX_VALUE;
	int boundSouth = Integer.MIN_VALUE;
	int boundWest = Integer.MAX_VALUE;
	int boundEast = Integer.MIN_VALUE;

	/**
	 * Adds the path information of the agent.
	 * 
	 * @param loc Location the agent moved to
	 */
	public void addPathStep(Location loc) {
		if (loc.v[0] < boundWest)
			boundWest = loc.v[0];
		if (loc.v[0] > boundEast)
			boundEast = loc.v[0];
		if (loc.v[1] > boundSouth)
			boundSouth = loc.v[1];
		if (loc.v[1] < boundNorth)
			boundNorth = loc.v[1];

		path.add(loc);
		if (path.size() > MAX_PATH_HISTORY) {
			path.remove(0);
		}
	}

	public void addRockBump() {
		++countRockBumps;
	}

	public void addSexPreg() {
		++sexualPregs;
	}

	/**
	 * Increments the number of steps the agent has taken.
	 */
	public void addStep() {
		++countSteps;
	}

	public void addTurn() {
		++countTurns;
	}

	/* Keep a total tally of the living agents */
	public void alive() {
		liveCount[type]++;
	}

	public void ate(int agentType) {
		eatenAgentsofType[agentType]++;
	}

	public int getAgentNumber() {
		return agentNumber;
	}

	public int getAgentType() {
		return type;
	}

	public long getDeathTick() {
		return deathTick;
	}

	public List<Location> getPathHistory() {
		return path;
	}

	public int getTypeNum() {
		return agentTypes;

	}


	public static void printAgentHeaders(PrintWriter pw) {
		// Concatenating the headers of the report file.
		pw.print("Agent Number");
		pw.print("\tAgent Type");
		pw.print("\tBirth");
		pw.print("\tParent1\tParent2");
		pw.print("\tDeath");
		pw.print("\tChildren");
		pw.print("\tSexual Pregnancies");
		pw.print("\tSteps");
		pw.print("\tTurns");
		pw.print("\tAgent Bumps");
		pw.print("\tRock Bumps");
		pw.print("\tStrategy");

		pw.print("\tboundNorth");
		pw.print("\tboundEast");
		pw.print("\tboundSouth");
		pw.print("\tboundWest");

		pw.println();
	}

	public void printInfo(java.io.PrintWriter pw) {
		pw.print(agentNumber);
		pw.print("\t" + (type + 1));
		pw.print("\t" + birthTick);
		pw.print("\t" + parent1);
		pw.print("\t" + parent2);
		pw.print("\t" + deathTick);


		//TODO: look into if this works/does anything useful
		if (deathTick != -1) {
			stepslivedAgentsofType[type] += (deathTick - birthTick);
			deadAgentsofType[type]++;
		} else {
			aliveAgentsofType[type]++;
		}

		pw.print("\t" + directChildren);
		pw.print("\t" + sexualPregs);

		int total = countSteps + countTurns + countAgentBumps + countRockBumps;
		totalsAgentsofType[type] += total;
		stepsAgentsofType[type] += countSteps;

		pw.print("\t" + countSteps);
		pw.print("\t" + countTurns);
		pw.print("\t" + countAgentBumps);
		pw.print("\t" + countRockBumps);

		if (cheater) {
			pw.print("\tcheat");
		} else {
			pw.print("\tcooperate");
		}

		pw.print("\t" + boundNorth);
		pw.print("\t" + boundEast);
		pw.print("\t" + boundSouth);
		pw.print("\t" + boundWest);

		pw.println();
	}

	public void setDeath(long death) {
		deathTick = death;
		path = null;
	}

	public void setStrategy(boolean cheat) {
		cheater = cheat;
	}

	public void useAgentBumpEnergy(int val) {
		agentBumpEnergies[type] += val;
	}

	public void useExtraEnergy(int val) {
		agentAgingEnergies[type] += val;
	}

	/* Other sources including energy gained from the agent strategy */
	public void useOthers(int val) {
		otherEnergySinks[type] += val;
	}

	public void useRockBumpEnergy(int val) {
		rockBumpEnergies[type] += val;
	}

	public void useStepEnergy(int val) {
		stepEnergies[type] += val;
	}

	public void useTurning(int val) {
		turningEnergies[type] += val;
	}

}
