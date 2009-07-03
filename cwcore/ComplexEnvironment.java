package cwcore;

import ga.GATracker;
import ga.GeneticCode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import cobweb.Agent;
import cobweb.ColorLookup;
import cobweb.Environment;
import cobweb.RandomNoGenerator;
import cobweb.TickScheduler;
import cobweb.TypeColorEnumeration;
import cobweb.UIInterface;
import driver.ControllerFactory;
import driver.Parser;

public class ComplexEnvironment extends Environment implements
		TickScheduler.Client {

	@SuppressWarnings("unused")
	private final String filePath = "report.xls"; // default report file path; may change in future

	public static final int FLAG_STONE = 1;

	public static final int FLAG_FOOD = 2;

	public static final int FLAG_AGENT = 3;

	public static final int FLAG_WASTE = 4;

	public static final int INIT_LAST_MOVE = 0; // Initial last move set to cooperate

	public static int PD_PAYOFF_REWARD;

	public static int PD_PAYOFF_SUCKER;

	public static int PD_PAYOFF_TEMPTATION;

	public static int PD_PAYOFF_PUNISHMENT;

	public static final int AGENT_TYPES = 4;

	public static final List<CommPacket> currentPackets = new ArrayList<CommPacket>();

	// Info for logfile
	private long tickCount = 0;

	private int agentTypeCount = 0;

	private static RandomNoGenerator environmentRandom;

	public int getAgentTypes() {
		return agentTypeCount;
	}

	/*
	 * All variables that will by referenced by the parser must be declared as
	 * 1-sized arrays as all of the following are.
	 */

	private String controllerName;

	private int[] w = new int[1];

	private int[] h = new int[1];

	private int[] randomStones = new int[1];

	private float[] maxFoodChance = new float[1];

	private boolean[] colorful = new boolean[1];

	private boolean[] wrapMap = new boolean[1];

	private boolean[] spawnNewAgents = new boolean[1];

	private boolean[] keepOldAgents = new boolean[1];

	private boolean[] dropNewFood = new boolean[1];

	private boolean[] keepOldArray = new boolean[1];

	private boolean[] prisDilemma = new boolean[1];

	private boolean[] foodWeb = new boolean[1];

	private boolean[] keepOldWaste = new boolean[1];

	private boolean[] keepOldPackets = new boolean[1];

	private long[] randomSeed = new long[1];

	private int[] numColor = new int[1];

	private int[] colorSelectSize = new int[1];

	private boolean[] newColorizer = new boolean[1];

	private int[] reColorTimeStep = new int[1];

	private int[] colorizerMode = new int[1];

	private boolean[] foodBstrat = new boolean[1];

	private int[] memory = new int[1];

	/*
	 * And variables that dependant on agent type are two dimensional arrays (1
	 * sized arrays of arrays of undefined size) as the following are.
	 */
	private float[][] foodRate = new float[1][];

	private int[][] foodGrow = new int[1][];

	private int[][] food = new int[1][];

	private int[][] agents = new int[1][];

	private int[][] foodEnergy = new int[1][];

	private int[][] otherFoodEnergy = new int[1][];

	private int[][] breedEnergy = new int[1][];

	private int[][] pregnancyPeriod = new int[1][];

	private int[][] initEnergy = new int[1][];

	private int[][] stepEnergy = new int[1][];

	private int[][] stepRockEnergy = new int[1][];

	private int[][] stepAgentEnergy = new int[1][];

	private int[][] turnRightEnergy = new int[1][];

	private int[][] turnLeftEnergy = new int[1][];

	private float[][] mutationRate = new float[1][];

	private int[][] memoryBits = new int[1][];

	private int[][] commSimMin = new int[1][];

	private int[][] communicationBits = new int[1][];

	private int[][] mode = new int[1][];

	private float[][] foodDeplete = new float[1][];

	private int[][] depleteTimeSteps = new int[1][];

	private float[][] sexualBreedChance = new float[1][];

	private float[][] asexualBreedChance = new float[1][];

	private float[][] breedSimMin = new float[1][];

	private int[][] sexualPregnancyPeriod = new int[1][];

	private boolean[][] agingMode = new boolean[1][10];

	private int[][] agingLimit = new int[1][10];

	private float[][] agingRate = new float[1][10];

	// waste parameters
	private boolean[][] wasteMode = new boolean[1][10];

	private int[][] wastePen = new int[1][10];

	private int[][] wasteGain = new int[1][10];

	private int[][] wasteLoss = new int[1][10];

	private float[][] wasteRate = new float[1][10];

	private int[][] wasteInit = new int[1][10];

	// PD
	private boolean[][] pdTitForTat = new boolean[1][10];

	private int[][] pdCoopProb = new int[1][10];

	private int[][] plants2eat = new int[1][];

	private int[][] agents2eat = new int[1][];

	private int[][] draught_period = new int[1][10];

	private int[][] agentPDStrategy = new int[1][10]; // Tit-for-tat or probability based. $$$$$ Can introduce more Prisoner's Dilemma strategies in the future

	private int[][] agentPDAction = new int[1][10]; // The agent's action; 1 == cheater, else cooperator

	private int[][] lastPDMove = new int[1][10];

	private boolean[][] broadcastMode = new boolean[1][10];

	private boolean[][] broadcastEnergyBased = new boolean[1][10];

	private int[][] broadcastFixedRange = new int[1][10];

	private int[][] broadcastEnergyMin = new int[1][10];

	private int[][] broadcastEnergyCost = new int[1][10];

	/** The default genetic sequence of agent type. */
	private String[][] genetic_sequence = new String[1][10];

	/*
	 * In order for the array sizing algorithms to function properly, all
	 * variables dependent on agent type must be listed inside of this array of
	 * objects.
	 */
	private Object[][] agentTypeCountDependents = { foodRate, foodGrow, food,
			agents, foodEnergy, otherFoodEnergy, breedEnergy, pregnancyPeriod,
			initEnergy, stepEnergy, stepRockEnergy, turnRightEnergy,
			turnLeftEnergy, mutationRate, memoryBits, mode, foodDeplete,
			depleteTimeSteps, commSimMin, stepAgentEnergy, sexualBreedChance,
			asexualBreedChance, breedSimMin, sexualPregnancyPeriod,
			communicationBits, genetic_sequence };

	private java.util.Hashtable<String, Object> parseData = new java.util.Hashtable<String, Object>();

	private Colorizer colorizer;

	private int foodTypeCount;

	public Colorizer getColorizer() {
		return colorizer;
	}

	@Override
	protected void finalize() {
		if (logStream != null)
			logStream.close();
	}

	/** Sets the default mutable variables of each agent type. */
	public void setDefaultMutableAgentParam() {
		ComplexAgent.setDefaultMutableParams(mutationRate[0], initEnergy[0], foodEnergy[0], otherFoodEnergy[0],
		breedEnergy[0], pregnancyPeriod[0], stepEnergy[0], stepRockEnergy[0], turnRightEnergy[0],
		turnLeftEnergy[0], commSimMin[0], stepAgentEnergy[0], sexualPregnancyPeriod[0], breedSimMin[0],
		sexualBreedChance[0], asexualBreedChance[0], agingLimit[0], agingRate[0], pdCoopProb[0],
		broadcastFixedRange[0], broadcastEnergyMin[0], broadcastEnergyCost[0]);
	}


	@Override
	public void load(cobweb.Scheduler s, Parser p/* java.io.Reader r */)
			throws java.io.IOException {
		// sFlag stores whether or not we are using a new scheduler
		boolean sFlag = (s != theScheduler);

		if (sFlag) {
			theScheduler = s;
			s.addSchedulerClient(this);
		}

		tickCount = theScheduler.getTime();

		int oldH = h[0];
		int oldW = w[0];

		/**
		 * the first parameter must be the number of agent types as we must
		 * allocate space to store the parameter for each type
		 */

		copyParamsFromParser(p);

		setDefaultMutableAgentParam();

		for (int i = 0; i < draught_period[0].length; i++) {
			// draught_period[0][i] = 10;
			draughtdays[i] = 0;
		}

		/**
		 * initArray is a useful utility that resizes an array to the given
		 * indices and copies over any old data that still falls within the new
		 * boundries. We use it to preserve the old agent-type dependant
		 * parameters while possibly making room for new ones.
		 */
		int[] indices = { 1, agentTypeCount };
		for (int i = 0; i < agentTypeCountDependents.length; ++i) {
			cobweb.ArrayUtilities.initArray(agentTypeCountDependents[i],
					indices, 0);
		}
		/** calls the parsing utility with our parsing hash-table parseData */
		/**
		 * try { cobweb.parseClass.parseLoad(r, "ComplexEnvironment.End",
		 * parseData); } catch (java.io.IOException e) { throw new
		 * java.io.IOException(); } catch (ArrayIndexOutOfBoundsException e) {
		 * throw new InstantiationError("Array out of bounds problem encountered
		 * while parsing ComplexEnvironment parameters. Most likely, the number
		 * of agents was not specified properly."); }
		 */
		/**
		 * If the random seed is set to 0 in the data file, it means we use the
		 * system time instead
		 */
		// $$$$$ This means setting Random Seed to zero would get non-repeatable results.  Apr 19


		if (randomSeed[0] == 0)
			randomSeed[0] = System.currentTimeMillis();

		cobweb.globals.random = new RandomNoGenerator(randomSeed[0]);
		environmentRandom = cobweb.globals.random;
		cobweb.globals.behaviorRandom = new RandomNoGenerator(42);


		if (keepOldArray[0]) {
			int[] boardIndices = { w[0], h[0] };
			array = new cobweb.ArrayEnvironment(w[0], h[0], array);
			foodarray = (int[][]) cobweb.ArrayUtilities.initArray(foodarray, boardIndices, 0);
		} else {
			array = new cobweb.ArrayEnvironment(w[0], h[0]);
			foodarray = new int[w[0]][h[0]];
		}

		if (wastearray == null || !keepOldWaste[0]) {
			loadNewWaste();
		}
		else {
			loadOldWaste();
		}

		loadFoodMode();

		if (keepOldAgents[0]) {
			loadOldAgents(sFlag, oldH, oldW);
		} else {
			killOldAgents();
		}


		if (keepOldPackets[0]) {
			// keep commPackets.list
		}

		// add a random amount of new stones
		for (int i = 0; i < randomStones[0]; ++i) {
			cobweb.Environment.Location l;
			int tries = 0;
			do {
				l = getRandomLocation();
			} while ((tries++ < 100)
					&& ((l.testFlag(ComplexEnvironment.FLAG_STONE) || l
							.testFlag(ComplexEnvironment.FLAG_WASTE)) && l
							.getAgent() == null));
			if (tries < 100)
				l.setFlag(ComplexEnvironment.FLAG_STONE, true);
		}

		// add random amounts of new food for each type
		if (dropNewFood[0]) {
			loadNewFood();
		}

		try {
			ControllerFactory.Init(controllerName);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		//spawn new random agents for each type
		if (spawnNewAgents[0]) {
			loadNewAgents();
		}
		// reinitialize the colorizer
		if (newColorizer[0] == true || colorizer == null) {
			colorizer = new Colorizer(numColor[0], colorSelectSize[0],
					colorizerMode[0]);

			colorizer.reColorAgents(getAgentCollection(), 1);

		}
	}

	private void loadNewFood() {
		for (int i = 0; i < food[0].length; ++i) {
			for (int j = 0; j < food[0][i]; ++j) {
				cobweb.Environment.Location l;
				int tries = 0;
				do {
					l = getRandomLocation();
				} while ((tries++ < 100)
						&& (l.testFlag(ComplexEnvironment.FLAG_STONE) || l
								.testFlag(ComplexEnvironment.FLAG_WASTE)));
				if (tries < 100)
					l.setFlag(ComplexEnvironment.FLAG_FOOD, true);
				setFoodType(l, i);
			}
		}
	}

	private void loadOldWaste() {
		Waste[][] oldWasteArray = wastearray;
		wastearray = new Waste[w[0]][h[0]];

		int height = Math.min(w[0], oldWasteArray.length);
		int width = Math.min(h[0], oldWasteArray[0].length);
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				wastearray[i][j] = oldWasteArray[i][j];

		// Add in-bounds old waste to the new scheduler and update new
		// constants
		for (Location currentPos = getLocation(0, 0); currentPos.v[1] < h[0]; ++currentPos.v[1]) {
			for (currentPos.v[0] = 0; currentPos.v[0] < w[0]; ++currentPos.v[0]) {
				if (wastearray[currentPos.v[0]][currentPos.v[1]] != null) {
					currentPos.setFlag(ComplexEnvironment.FLAG_FOOD, false);
					currentPos.setFlag(ComplexEnvironment.FLAG_STONE, false);
					currentPos.setFlag(ComplexEnvironment.FLAG_WASTE, true);
				}
			}
		}
	}

	private void loadNewWaste() {
		wastearray = new Waste[w[0]][h[0]];

		for (Location pos = getLocation(0, 0); pos.v[1] < h[0]; pos.v[1]++)
			for (pos.v[0] = 0; pos.v[0] < w[0]; pos.v[0]++)
				pos.setFlag(FLAG_WASTE, false);
	}

	private void loadNewAgents() {
		int action = -1;  // $$$$$ -1: not play Prisoner's Dilemma
		for (int i = 0; i < agents[0].length; ++i) {
			double coopProb = pdCoopProb[0][i]/100.0d; // static value for now $$$$$$ added for the below else block.  Apr 18

		    lastPDMove[0][i] = -1; // $$$$$$ initial for the new agents.  Apr 18
		    if (pdTitForTat[0][i]) { agentPDStrategy[0][i] = 1;}     // $$$$$$ if tit-for-tat, then agentPDStrategy == 1.  Apr 18

			for (int j = 0; j < agents[0][i]; ++j) {
				if (prisDilemma[0]) {
					if (pdTitForTat[0][i]) {
						action = 0; // spawn cooperating agent
					} else {
						// $$$$$$ change from the first silent line to the following three.  Apr 18
						//action = 1;
						action = 0;
					    float rnd = cobweb.globals.behaviorRandom.nextFloat();
					    //System.out.println("rnd: " + rnd);
					    //System.out.println("coopProb: " + coopProb);
					    if (rnd > coopProb) action = 1; // agent defects depending on probability
					}

				}

				cobweb.Environment.Location l;
				int tries = 0;
				do {
					l = getRandomLocation();
				} while ((tries++ < 100) && ((l.getAgent() != null) // don't
																	// spawn
																	// on
																	// top
																	// of
																	// agents
						|| l.testFlag(ComplexEnvironment.FLAG_STONE) // nor
																		// on
																		// stone
																		// tiles
				|| l.testFlag(ComplexEnvironment.FLAG_WASTE))); // nor on
																// waste
																// tiles
				if (tries < 100) {
					int agentType = i;
					new ComplexAgent(agentType, l, action, memory[0],
							foodBstrat[0], initEnergy[0][i],
							foodEnergy[0][i], otherFoodEnergy[0][i],
							breedEnergy[0][i], pregnancyPeriod[0][i],
							stepEnergy[0][i], stepRockEnergy[0][i],
							turnRightEnergy[0][i], turnLeftEnergy[0][i],
							mutationRate[0][i], colorful[0],
							memoryBits[0][i], commSimMin[0][i],
							stepAgentEnergy[0][i], sexualBreedChance[0][i],
							asexualBreedChance[0][i], breedSimMin[0][i],
							sexualPregnancyPeriod[0][i],
							communicationBits[0][i], agingMode[0][i],
							agingLimit[0][i], agingRate[0][i],
							wasteMode[0][i], wastePen[0][i],
							wasteGain[0][i], wasteLoss[0][i],
							wasteRate[0][i], wasteInit[0][i],
							pdTitForTat[0][i], pdCoopProb[0][i],
							broadcastMode[0][i],
							broadcastEnergyBased[0][i],
							broadcastFixedRange[0][i],
							broadcastEnergyMin[0][i],
							broadcastEnergyCost[0][i], plants2eat[i],
							agents2eat[i], agentPDStrategy[0][i],   // Tit-for-tat or probability based
							agentPDAction[0][i], // The agent's action;  1 == cheater, else cooperator
							lastPDMove[0][i], // Remember the opponent's
												// move in the last game
							genetic_sequence[0][i]); // Default genetic
														// sequence of agent
														// type
				}
			}
		}
	}

	private void killOldAgents() {
		// if keepOldAgents is false then we want to kill all of the agents
		for (Agent a : new LinkedList<Agent>(getAgentCollection())) {
			if (a.isAlive()) {
				a.die();
			}
		}
		// This second line may seem redundant, but it fact its not.
		// By reseting the hashtable, we remove a bit of non-determinance
		// due to the way
		// hashtable is implemented. If you remove this line, the coloring
		// will not be entirely consistant
		// between simulation runs based on the same seed and parameters (a
		// bad thing)
		clearAgents();
	}

	private void loadOldAgents(boolean sFlag, int oldH, int oldW) {
		// Add in-bounds old agents to the new scheduler and update new
		// constants
		Location currentPos = getLocation(0, 0);
		for (; currentPos.v[1] < h[0]; ++currentPos.v[1]) {
			for (currentPos.v[0] = 0; currentPos.v[0] < w[0]; ++currentPos.v[0]) {
				if (currentPos.getAgent() != null) {
					// we only need to add the agent if the scheduler is
					// new, otherwise we assume it already belongs to the
					// scheduler
					if (sFlag) {
						getScheduler().addSchedulerClient(
								currentPos.getAgent());
					}
					int theType = ((ComplexAgent) currentPos.getAgent())
							.getAgentType();
					((ComplexAgent) currentPos.getAgent()).setConstants(
							theType, ((ComplexAgent) currentPos.getAgent())
									.getAgentPDAction(), memory[0],
							foodBstrat[0], initEnergy[0][theType],
							foodEnergy[0][theType],
							otherFoodEnergy[0][theType],
							breedEnergy[0][theType],
							pregnancyPeriod[0][theType],
							stepEnergy[0][theType],
							stepRockEnergy[0][theType],
							turnRightEnergy[0][theType],
							turnLeftEnergy[0][theType],
							mutationRate[0][theType], colorful[0],
							commSimMin[0][theType],
							stepAgentEnergy[0][theType],
							sexualBreedChance[0][theType],
							asexualBreedChance[0][theType],
							breedSimMin[0][theType],
							sexualPregnancyPeriod[0][theType],
							agingMode[0][theType], agingLimit[0][theType],
							agingRate[0][theType], wasteMode[0][theType],
							wastePen[0][theType], wasteGain[0][theType],
							wasteLoss[0][theType], wasteRate[0][theType],
							wasteInit[0][theType], pdTitForTat[0][theType],
							pdCoopProb[0][theType],
							broadcastMode[0][theType],
							broadcastEnergyBased[0][theType],
							broadcastFixedRange[0][theType],
							broadcastEnergyMin[0][theType],
							broadcastEnergyCost[0][theType],
							plants2eat[theType], agents2eat[theType],
							agentPDStrategy[0][theType],  // Tit-for-tat or probability based
							agentPDAction[0][theType], // The agent's action; 1 == cheater, else cooperator
													   //  $$$$$ this parameter seems useless, there is another PDaction parameter above already.  Apr 18
							lastPDMove[0][theType], // Remember the
													// opponent's move in
													// the last game
							genetic_sequence[0][theType]); // Default
															// genetic
															// sequence of
															// agent type
				}
			}
		}
		// Remove agents that have fallen out of bounds
		for (currentPos.v[1] = 0; currentPos.v[1] < oldH; ++currentPos.v[1]) {
			for (currentPos.v[0] = w[0]; currentPos.v[0] < oldW; ++currentPos.v[0]) {
				if (currentPos.getAgent() != null)
					currentPos.getAgent().die();
			}
		}
		for (currentPos.v[1] = h[0]; currentPos.v[1] < oldH; ++currentPos.v[1]) {
			for (currentPos.v[0] = 0; currentPos.v[0] < oldW
					&& currentPos.v[0] < w[0]; ++currentPos.v[0]) {
				if (currentPos.getAgent() != null)
					currentPos.getAgent().die();
			}
		}
	}

	private void loadFoodMode() {
		for (int i = 0; i < mode[0].length; ++i) {
			switch (mode[0][i]) {

			case 1:
				foodDeplete[0][i] = environmentRandom.nextFloat() * 0.2f + 0.3f;
				depleteTimeSteps[0][i] = environmentRandom.nextIntRange(20,
						40);
				break;

			default:
				if (foodDeplete[0][i] < 0.0f || foodDeplete[0][i] > 1.0f)
					foodDeplete[0][i] = environmentRandom.nextFloat();
				if (depleteTimeSteps[0][i] <= 0)
					depleteTimeSteps[0][i] = environmentRandom.nextInt(100) + 1;
				break;
			}
		}
	}

	private void copyParamsFromParser(Parser p) {
		controllerName = p.ControllerName;
		agentTypeCount = p.AgentCount[0];
		foodTypeCount = p.FoodCount[0];
		w[0] = p.Width[0];
		h[0] = p.Height[0];
		randomStones[0] = p.randomStones[0];
		maxFoodChance[0] = p.maxFoodChance[0];
		colorful[0] = p.ColorCodedAgents[0];
		wrapMap[0] = p.wrap[0];
		spawnNewAgents[0] = p.spawnNewAgents[0];
		keepOldAgents[0] = p.keepOldAgents[0];
		dropNewFood[0] = p.dropNewFood[0];
		keepOldArray[0] = p.keepOldArray[0];
		randomSeed[0] = p.RandomSeed[0];
		numColor[0] = p.numColor[0];
		colorSelectSize[0] = p.colorSelectSize[0];
		newColorizer[0] = p.newColorizer[0];
		keepOldWaste[0] = p.keepOldWaste[0];
		keepOldPackets[0] = p.keepOldPackets[0];
		reColorTimeStep[0] = p.reColorTimeStep[0];
		colorizerMode[0] = p.colorizerMode[0];
		prisDilemma[0] = p.PrisDilemma[0];
		foodWeb[0] = p.FoodWeb[0];
		foodBstrat[0] = p.food_bias[0];
		memory[0] = p.memory_size[0];

		foodRate = p.foodRate;
		foodGrow = p.foodGrow;
		food = p.food;
		agents = p.agents;
		foodEnergy = p.foodEnergy;
		otherFoodEnergy = p.otherFoodEnergy;
		breedEnergy = p.breedEnergy;
		pregnancyPeriod = p.pregnancyPeriod;
		initEnergy = p.initEnergy;
		stepEnergy = p.stepEnergy;
		stepRockEnergy = p.stepRockEnergy;
		stepAgentEnergy = p.stepAgentEnergy;
		turnRightEnergy = p.turnRightEnergy;
		turnLeftEnergy = p.turnLeftEnergy;
		mutationRate = p.mutationRate;
		memoryBits = p.memoryBits;
		commSimMin = p.commSimMin;
		communicationBits = p.communicationBits;
		draught_period = p.draught;
		mode = p.mode;
		foodDeplete = p.foodDeplete;
		depleteTimeSteps = p.depleteTimeSteps;
		sexualBreedChance = p.sexualBreedChance;
		asexualBreedChance = p.asexualBreedChance;
		breedSimMin = p.breedSimMin;
		sexualPregnancyPeriod = p.sexualPregnancyPeriod;
		agingMode = p.agingMode;
		agingLimit = p.agingLimit;
		agingRate = p.agingRate;
		wasteMode = p.wasteMode;
		wastePen = p.wastePen;
		wasteGain = p.wasteGain;
		wasteLoss = p.wasteLoss;
		wasteRate = p.wasteRate;
		wasteInit = p.wasteInit;
		pdTitForTat = p.pdTitForTat;
		pdCoopProb = p.pdCoopProb;
		plants2eat = p.plants2eat;
		agents2eat = p.agents2eat;
		broadcastMode = p.broadcastMode;
		broadcastEnergyBased = p.broadcastEnergyBased;
		broadcastFixedRange = p.broadcastFixedRange;
		broadcastEnergyMin = p.broadcastEnergyMin;
		broadcastEnergyCost = p.broadcastEnergyCost;
		genetic_sequence = p.genetic_sequence;

		PD_PAYOFF_REWARD = p.reward[0];
		PD_PAYOFF_TEMPTATION = p.temptation[0];
		PD_PAYOFF_SUCKER = p.sucker[0];
		PD_PAYOFF_PUNISHMENT = p.punishment[0];
	}

	/* JUST ADDED */
	@Override
	public synchronized void selectStones(int x, int y, cobweb.UIInterface theUI) {
		cobweb.Environment.Location l;
		l = getUserDefinedLocation(x, y);
		if (l.testFlag(ComplexEnvironment.FLAG_STONE)) {
			l.setFlag(ComplexEnvironment.FLAG_STONE, false);
			theUI.writeToTextWindow("Stone removed at location (" + x + "," + y
					+ ")\n");
		} else if (l.getAgent() == null
				&& !l.testFlag(ComplexEnvironment.FLAG_FOOD)
				&& !l.testFlag(ComplexEnvironment.FLAG_STONE)) {
			l.setFlag(ComplexEnvironment.FLAG_STONE, true);
			theUI.writeToTextWindow("Stone added at location (" + x + "," + y
					+ ")\n");
		} else {
			return;
		}
		java.awt.Color[] tileColors = new java.awt.Color[getSize(AXIS_X)
				* getSize(AXIS_Y)];
		fillTileColors(tileColors);
		theUI.newTileColors(getSize(AXIS_X), getSize(AXIS_Y), tileColors);
	}

	@Override
	public synchronized void selectFood(int x, int y, int type, cobweb.UIInterface theUI) {

		cobweb.Environment.Location l;
		l = getUserDefinedLocation(x, y);
		if (l.testFlag(ComplexEnvironment.FLAG_FOOD) && getFoodType(l) == type) {  // $$$$$$ add " && getFoodType(l) == type"  Apr 3
			l.setFlag(ComplexEnvironment.FLAG_FOOD, false);
			theUI.writeToTextWindow("Food removed at location (" + x + "," + y
					+  "): type " + (type + 1) + "\n");  // $$$$$$ change from "+ ")\n");"   Apr 3
		} else if (//l.getAgent() == null &&     //  $$$$$$ silence this condition, the food would be added under the agent.  Apr 3
				   !(l.testFlag(ComplexEnvironment.FLAG_FOOD))
				&& !(l.testFlag(ComplexEnvironment.FLAG_STONE))) {
			theUI.writeToTextWindow("Food added at location (" + x + "," + y
					+ "): type " + (type + 1) + "\n");
			l.setFlag(ComplexEnvironment.FLAG_FOOD, true);
			setFoodType(l, type);
		}
		java.awt.Color[] tileColors = new java.awt.Color[getSize(AXIS_X)
				* getSize(AXIS_Y)];
		fillTileColors(tileColors);
		theUI.newTileColors(getSize(AXIS_X), getSize(AXIS_Y), tileColors);
	}

	@Override
	public synchronized void selectAgent(int x, int y, int type, cobweb.UIInterface theUI) {
		int action = -1;  // $$$$$ -1: not play Prisoner's Dilemma
		cobweb.Environment.Location l;
		l = getUserDefinedLocation(x, y);
		if (l.getAgent() != null && l.getAgent().type() == type) {  // &&&&&& add " && l.getAgent().type() == type"   Apr 3
			l.getAgent().die();
			theUI.writeToTextWindow("Agent removed at location (" + x + "," + y +  "): type " + (type + 1) + "\n");  // $$$$$$ added on Apr 3
		} else if (type < agents[0].length) {
			if ((l.getAgent() == null)
					&& !l.testFlag(ComplexEnvironment.FLAG_STONE)
					&& !l.testFlag(ComplexEnvironment.FLAG_WASTE)) {
				int agentType = type;
				if (prisDilemma[0]) {
					// System.out.println("in select agent: Value of PrisDilemma
					// is true");
					// System.out.println("PrisDilemma: "+prisDilemma[0]);

					action = 0;

					lastPDMove[0][agentType] = -1;  // &&&&&& initial for this new agent.  Apr 18
				    if (pdTitForTat[0][agentType]) { agentPDStrategy[0][agentType] = 1;}     // $$$$$$ if tit-for-tat, then agentPDStrategy == 1.  Apr 18


					double coopProb = pdCoopProb[0][agentType]/100.0d; // static value for now $$$$$$ added for the below else block.
				    float rnd = cobweb.globals.behaviorRandom.nextFloat();
				    //System.out.println("rnd: " + rnd);
				    //System.out.println("coopProb: " + coopProb);

				    if (rnd > coopProb) action = 1; // agent defects depending on probability
				}
				// spammy
				// System.out.println("type "+agentType);
				new ComplexAgent(agentType, l, action, memory[0],
						foodBstrat[0], initEnergy[0][agentType],
						foodEnergy[0][agentType],
						otherFoodEnergy[0][agentType],
						breedEnergy[0][agentType],
						pregnancyPeriod[0][agentType],
						stepEnergy[0][agentType], stepRockEnergy[0][agentType],
						turnRightEnergy[0][agentType],
						turnLeftEnergy[0][agentType],
						mutationRate[0][agentType], colorful[0],
						memoryBits[0][agentType], commSimMin[0][agentType],
						stepAgentEnergy[0][agentType],
						sexualBreedChance[0][agentType],
						asexualBreedChance[0][agentType],
						breedSimMin[0][agentType],
						sexualPregnancyPeriod[0][agentType],
						communicationBits[0][agentType],
						agingMode[0][agentType], agingLimit[0][agentType],
						agingRate[0][agentType], wasteMode[0][agentType],
						wastePen[0][agentType], wasteGain[0][agentType],
						wasteLoss[0][agentType], wasteRate[0][agentType],
						wasteInit[0][agentType], pdTitForTat[0][agentType],
						pdCoopProb[0][agentType], broadcastMode[0][agentType],
						broadcastEnergyBased[0][agentType],
						broadcastFixedRange[0][agentType],
						broadcastEnergyMin[0][agentType],
						broadcastEnergyCost[0][agentType],
						plants2eat[agentType], agents2eat[agentType],
						agentPDStrategy[0][agentType],  // Tit-for-tat or probability based
						agentPDAction[0][agentType], // The agent's action; 1 == cheater, else cooperator
						lastPDMove[0][agentType], // Remember the opponent's
													// move in the last game);
						genetic_sequence[0][agentType]); // Default genetic
															// sequence of agent
															// type
				theUI.writeToTextWindow("Agent added at location (" + x + "," + y +  "): type " + (agentType + 1) + "\n");  // $$$$$$ added on Apr 3
			}
		}
	}

	/*
	 * Remove components from the environment mode 0 : remove all the components
	 * mode -1: remove stones mode -2: remove food mode -3: remove agents mode
	 * -4: remove waste
	 */
	@Override
	public void remove(int mode, cobweb.UIInterface ui) {
		Location currentPos = getLocation(0, 0);
		for (; currentPos.v[1] < getSize(AXIS_Y); ++currentPos.v[1]) {
			for (currentPos.v[0] = 0; currentPos.v[0] < getSize(AXIS_X); ++currentPos.v[0]) {
				/*
				 * To ensure everything works properly, we do the removal
				 * according to each component's behaviour. - stone tiles cannot
				 * coexist with waste tiles nor food tiles and agent tiles. -
				 * waste tiles cannot coexist with stone tiles nor food tiles
				 * and agent tiles. - an agent can be on top of an empty tile
				 * and a food tile only - a food tile can exist on its own
				 */
				if (currentPos.testFlag(FLAG_STONE)) {
					if ((mode == 0) || (mode == -1))
						currentPos
								.setFlag(ComplexEnvironment.FLAG_STONE, false);
				} else if (currentPos.testFlag(FLAG_WASTE)) {
					if ((mode == 0) || (mode == -4)) {
						currentPos
								.setFlag(ComplexEnvironment.FLAG_WASTE, false);
						wastearray[currentPos.v[0]][currentPos.v[1]] = null;
					}
				} else {
					if (currentPos.testFlag(FLAG_FOOD)) {
						if ((mode == 0) || (mode == -2))
							currentPos.setFlag(ComplexEnvironment.FLAG_FOOD,
									false);
					}
					if (currentPos.getAgent() != null) {
						if ((mode == 0) || (mode == -3))
							currentPos.getAgent().die();
					}
				}
			}
		}
	}

	/*
	 * save copies all current parsable parameters, and their values, to the
	 * specified writer
	 */
	@Override
	public void save(java.io.Writer w) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w);
		pw.println(this.getClass().getName() + " " + agentTypeCount);
		pw.println("");

		int[] indices = new int[512];

		int length = parseData.size();
		String[] names = new String[length];
		Object[] saveArray = new Object[length];
		java.util.Enumeration<String> nameList = parseData.keys();
		java.util.Enumeration<Object> saveList = parseData.elements();

		for (int i = 0; saveList.hasMoreElements()
				&& nameList.hasMoreElements(); ++i) {
			names[i] = nameList.nextElement();
			saveArray[i] = java.lang.reflect.Array.get(saveList.nextElement(),
					0);
		}

		try {
			cobweb.parseClass.parseSave(pw, saveArray, names, indices, 0);
		} catch (java.io.IOException e) {
			// throw new java.io.IOException();
		}

		pw.println(this.getClass().getName() + ".End");
		pw.println("");
		pw.flush();
	}

	/**
	 * tickNotification is the method called by the scheduler each of its
	 * clients for every tick of the simulation. For environment,
	 * tickNotification performs all of the per-tick tasks necessary for the
	 * environment to function properly. These tasks include managing food
	 * depletion, food growth, and random food-"dropping".
	 */
	public void tickNotification(long tick) {

		++tickCount;
		if (reColorTimeStep[0] != 0 && tickCount % reColorTimeStep[0] == 0) {
			// colorizer = new Colorizer( numColor[0], colorSelectSize[0],
			// colorizerMode[0] );
			colorizer.reColorAgents(getAgentCollection(), colorizerMode[0]);
		}
		updateWaste();

		// for each agent type, we test to see if its deplete time step has
		// come, and if so deplete the food random
		// by the appropriate percentage
		for (int i = 0; i < agentTypeCount; ++i) {
			if (foodDeplete[0][i] != 0.0f && foodGrow[0][i] > 0
					&& (tickCount % depleteTimeSteps[0][i]) == 0) {
				depleteFood(i);
			}
		}

		boolean shouldGrow = false;
		for (int i = 0; i < agentTypeCount; ++i) {
			if (foodGrow[0][i] > 0) {
				shouldGrow = true;
				break;
			}
		}

		// if no food is growing (total == 0) this loop is not nessesary
		if (shouldGrow) {
			growFood();
		}

		// Air-drop food into the environment
		for (int i = 0; i < foodRate[0].length; ++i) {
			if (draughtdays[i] == 0) {
				dropFood(i);
			} else {
				draughtdays[i]--;
			}
		}

		if (observedAgent != null && !observedAgent.isAlive()) {
			observedAgent = null;
		}
	}

	private void dropFood(int type) {
		float foodDrop = foodRate[0][type];
		while (environmentRandom.nextFloat() < foodDrop) {
			--foodDrop;
			cobweb.Environment.Location l;
			int j = 0;
			do {
				++j;
				l = getRandomLocation();

			} while (j < foodGrow[0][type]
					&& (l.testFlag(ComplexEnvironment.FLAG_STONE)
							|| l.testFlag(ComplexEnvironment.FLAG_FOOD)
							|| l
									.testFlag(ComplexEnvironment.FLAG_WASTE) || l
							.getAgent() != null));

			l.setFlag(ComplexEnvironment.FLAG_FOOD, true);
			setFoodType(l, type);
		}
	}

	private void growFood() {
		// create a new ArrayEnvironment and a new food type array
		cobweb.ArrayEnvironment newArray = new cobweb.ArrayEnvironment(
				array.getSize(AXIS_X), array.getSize(AXIS_Y));
		int[][] newFoodArray = new int[w[0]][h[0]];
		// loop through all positions
		Location currentPos = getLocation(0, 0);
		for (; currentPos.v[1] < getSize(AXIS_Y); ++currentPos.v[1]) {
			for (currentPos.v[0] = 0; currentPos.v[0] < getSize(AXIS_X); ++currentPos.v[0]) {
				// if theres a stone or already food, we simply copy the
				// information from the old arrays to the new ones
				if (currentPos.testFlag(ComplexEnvironment.FLAG_FOOD)
						|| currentPos
								.testFlag(ComplexEnvironment.FLAG_WASTE)
						|| currentPos
								.testFlag(ComplexEnvironment.FLAG_STONE)) {
					newArray.setLocationBits(currentPos, array
							.getLocationBits(currentPos));
					newFoodArray[currentPos.v[0]][currentPos.v[1]] = foodarray[currentPos.v[0]][currentPos.v[1]];
				}
				// otherwise, we want to see if we should grow food here
				else {

					// the following code block tests all adjacent squares
					// to this one and counts how many have food
					// as well how many of each food type exist

					double foodCount = 0;
					int mostFood[] = new int[agentTypeCount];
					for (int i = 0; i < agentTypeCount; ++i)
						mostFood[i] = 0;

					Location checkPos = currentPos
							.getAdjacent(DIRECTION_NORTH);
					if (checkPos != null
							&& checkPos
									.testFlag(ComplexEnvironment.FLAG_FOOD)) {
						++foodCount;
						++mostFood[getFoodType(checkPos)];
					}
					checkPos = currentPos.getAdjacent(DIRECTION_SOUTH);
					if (checkPos != null
							&& checkPos
									.testFlag(ComplexEnvironment.FLAG_FOOD)) {
						++foodCount;
						++mostFood[getFoodType(checkPos)];
					}
					checkPos = currentPos.getAdjacent(DIRECTION_EAST);
					if (checkPos != null
							&& checkPos
									.testFlag(ComplexEnvironment.FLAG_FOOD)) {
						++foodCount;
						++mostFood[getFoodType(checkPos)];
					}
					checkPos = currentPos.getAdjacent(DIRECTION_WEST);
					if (checkPos != null
							&& checkPos
									.testFlag(ComplexEnvironment.FLAG_FOOD)) {
						++foodCount;
						++mostFood[getFoodType(checkPos)];
					}

					// and if we have found any adjacent food, theres a
					// chance we want to grow food here
					if (foodCount > 0) {

						int max = 0;
						int growMe;

						// find the food that exists in the largest quantity
						for (int i = 1; i < mostFood.length; ++i)
							if (mostFood[i] > mostFood[max])
								max = i;

						// give the max food an extra chance to be chosen
						/*
						 * if( maxFoodChance[0] >
						 * cobweb.globals.random.nextFloat() ) { growMe =
						 * max; } //if not the max food, then we want to
						 * pick randomly from all the types else {
						 */
						growMe = environmentRandom.nextInt(foodTypeCount);
						// }

						// finally, we grow food according to a certain
						// amount of random chance
						if (foodCount * foodGrow[0][growMe] > 100 * environmentRandom.nextFloat()) {
							newArray.setLocationBits(currentPos, FOOD_CODE);
							// setFoodType (currentPos, growMe);
							newFoodArray[currentPos.v[0]][currentPos.v[1]] = growMe;
						}
					}
				}

			}
		}
		// The tile array we've just computed becomes the current tile array
		array = newArray;
		foodarray = newFoodArray;
	}

	private void depleteFood(int type) {
		// the algorithm for randomly selecting the food cells to delete
		// is as follows:
		// We iterate through all of the cells and the location of each
		// one containing food type i is added to
		// a random position in our vector. We then calculate exactly
		// how many food items we need to destroy, say N,
		// and we destroy the food at the positions occupying the last N
		// spots in our vector
		Vector<Location> locations = new Vector<Location>();
		for (int x = 0; x < getSize(AXIS_X); ++x)
			for (int y = 0; y < getSize(AXIS_Y); ++y) {
				Location currentPos = getLocation(x, y);
				if (currentPos.testFlag(ComplexEnvironment.FLAG_FOOD)
						&& getFoodType(currentPos) == type)
					locations.add(environmentRandom.nextInt(locations.size() + 1), currentPos);
			}

		int foodToDeplete = (int) (locations.size() * foodDeplete[0][type]);

		for (int j = 0; j < foodToDeplete; ++j) {
			Location loc = locations.remove(locations.size() - 1);
			loc.setFlag(ComplexEnvironment.FLAG_FOOD, false);
		}
		draughtdays[type] = draught_period[0][type];
	}

	private void updateWaste() {
		for (int i = 0; i < wastearray[0].length; i++) {
			for (int j = 0; j < wastearray[1].length; j++) {
				Location l = getLocation(i, j);
				if (l.testFlag(ComplexEnvironment.FLAG_WASTE) == false)
					continue;
				if (!wastearray[i][j].isActive(getTickCount())) {
					l.setFlag(ComplexEnvironment.FLAG_WASTE, false);
					wastearray[l.v[0]][l.v[1]] = null; // consider deactivating
														// and not deleting
				}
			}
		}
	}

	private static ColorLookup colorMap = TypeColorEnumeration.getInstance();

	@Override
	public void setclick(int click) {
		clickcount = click;
	}

	private static java.awt.Color wasteColor = new java.awt.Color(204, 102, 0);

	@Override
	public void fillTileColors(java.awt.Color[] tileColors) {
		Location currentPos = getLocation(0, 0);
		int tileIndex = 0;
		for (; currentPos.v[1] < getSize(AXIS_Y); ++currentPos.v[1]) {
			for (currentPos.v[0] = 0; currentPos.v[0] < getSize(AXIS_X); ++currentPos.v[0]) {
				if (currentPos.testFlag(FLAG_STONE))
					tileColors[tileIndex++] = java.awt.Color.darkGray;
				else if (currentPos.testFlag(FLAG_WASTE))
					tileColors[tileIndex++] = wasteColor;
				else if (currentPos.testFlag(FLAG_FOOD)) {
					tileColors[tileIndex++] = colorMap.getColor(
							getFoodType(currentPos), 0 /* agentTypeCount */);
				} else
					tileColors[tileIndex++] = java.awt.Color.white;
			}
		}
	}

	@Override
	public void log(java.io.Writer w) {
		logStream = new java.io.PrintWriter(w, false);
		writeLogTitles();
	}

	public static void trackAgent(java.io.Writer w) {
		ComplexAgent.setPrintWriter(w);
		ComplexAgent.tracked();
	}

	@Override
	public void report(java.io.Writer w) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w, false);

		printAgentInfo(pw);
	}

	/* Return foodCount (long) of all types of food */
	private long countFoodTiles() {
		long foodCount = 0;
		Location currentPos = getLocation(0, 0);
		for (; currentPos.v[1] < getSize(AXIS_Y); ++currentPos.v[1])
			for (currentPos.v[0] = 0; currentPos.v[0] < getSize(AXIS_X); ++currentPos.v[0])
				if (currentPos.testFlag(ComplexEnvironment.FLAG_FOOD))
					++foodCount;
		return foodCount;
	}

	/* Return foodCount (long) for a specific foodType (int) */
	private int countFoodTiles(int foodType) {
		int foodCount = 0;
		Location currentPos = getLocation(0, 0);
		for (; currentPos.v[1] < getSize(AXIS_Y); ++currentPos.v[1])
			for (currentPos.v[0] = 0; currentPos.v[0] < getSize(AXIS_X); ++currentPos.v[0])
				if (currentPos.testFlag(ComplexEnvironment.FLAG_FOOD))
					if (getFoodType(currentPos) == foodType)
						++foodCount;
		return foodCount;
	}

	/* Return totalEnergy of all agents */
	private long countAgentEnergy() {
		long totalEnergy = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			totalEnergy += agent.getEnergy();
		}
		return totalEnergy;
	}

	/* Return the average gene status of all 3 genes for all living agents */
	private double[][] getAvgGeneStatus() {
		double[][] avg_gene_status = new double[AGENT_TYPES][GeneticCode.NUM_GENES];
		for (int i = 0; i < AGENT_TYPES; i++) {
			for (int j = 0; j < GeneticCode.NUM_GENES; j++) {
				if (GATracker.total_agents[i] > 0) {
					avg_gene_status[i][j]
					  = new Double(Math.round( // Round this off to 4 decimal digits
							  GATracker.total_gene_status[i][j]/GATracker.total_agents[i]*10000)
							  )/10000;
				} else {
					avg_gene_status[i][j] = -1;
				}
			}
		}
		return avg_gene_status;
	}

	/* Return totalEnergy (long) for a certain agentType (int) */
	private long countAgentEnergy(int agentType) {
		long totalEnergy = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			if (agent.getAgentType() == agentType)
				totalEnergy += agent.getEnergy();
		}
		return totalEnergy;
	}

	private int[] numAgentsStrat(int agentType) {
		int stratArray[] = new int[2];
		int cheaters = 0;
		int coops = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			if (agent.getAgentType() == agentType
					&& agent.getAgentPDAction() == 0) {
				coops++;
				stratArray[0] = coops;
			} else if (agent.getAgentType() == agentType
					&& agent.getAgentPDAction() == 1) {
				cheaters++;
				stratArray[1] = cheaters;
			}

		}
		return stratArray;
	}

	private int[] numAgentsStrat() {
		int stratArray[] = new int[2];
		int cheaters = 0;
		int coops = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			if (agent.getAgentPDAction() == 0) {
				coops++;
				stratArray[0] = coops;
			} else if (agent.getAgentPDAction() == 1) {  // $$$$$$ add " if (agent.getAgentPDAction() == 1) ".  Apr 19
				cheaters++;
				stratArray[1] = cheaters;
			}

		}
		return stratArray;
	}

	/* Write the Log titles to the file,(called by log (java.io.Writer w)) */
	public void writeLogTitles() {
		if (logStream != null) {
			for (int i = 0; i < agentTypeCount; i++) {
				//logStream.print("\t\t" + "Type" + (i + 1) + "\t\t\t\t");  // $$$$$ why this format?
				logStream.print("\t" + "Type" + (i + 1) + "\t\t\t\t\t\t\t\t\t");  // $$$$$$ change to this on Apr 19
			}
			//logStream.print("\t\t\t" + "Total For all Agent types" + "\t\t");  // $$$$$ why this format?
			logStream.print("\t" + "Total for all Agent Types");  // $$$$$$ change to this on Apr 19
			logStream.println();
			logStream.println(); // $$$$$$ add a new line.  Apr 19
			for (int i = 0; i < agentTypeCount; i++) {

				logStream.print("Tick\t");
				logStream.print("FoodCount\t");
				logStream.print("AgentCount\t");
				logStream.print("AveAgentEnergy\t");
				logStream.print("AgentEnergy\t");
				logStream.print("Num. Cheat\t");
				logStream.print("Num. Coop\t");
				logStream.print("AvgRedGeneState\t");
				logStream.print("AvgGreenGeneState\t");
				logStream.print("AvgBlueGeneState\t");
			}
			// One final round of output for total
			logStream.print("Tick\t");
			logStream.print("FoodCount\t");
			logStream.print("AgentCount\t");
			logStream.print("AveAgentEnergy\t");
			logStream.print("AgentEnergy\t");
			logStream.print("Num. Cheat\t");
			logStream.print("Num. Coop\t");
			logStream.println();
			logStream.println();
		}
	}

	/* Return the current tick number, tickCount (long) */
	public long getTickCount() {
		return tickCount;
	}

	/* $$$$$$ Set the current tick number, tickCount (long).  Apr 19*/
	public void setTickCount(long tick) {
		tickCount = tick;
	}

	public ComplexAgentInfo addAgentInfo(int agentT, int action) {
		return addAgentInfo(new ComplexAgentInfo(getInfoNum(), agentT,
				tickCount, action));
	}

	public ComplexAgentInfo addAgentInfo(int agentT, ComplexAgentInfo p1,
			int action) {
		return addAgentInfo(new ComplexAgentInfo(getInfoNum(), agentT,
				tickCount, p1, action));
	}

	public ComplexAgentInfo addAgentInfo(int agentT, ComplexAgentInfo p1,
			ComplexAgentInfo p2, int action) {
		return addAgentInfo(new ComplexAgentInfo(getInfoNum(), agentT,
				tickCount, p1, p2, action));
	}

	public ComplexAgentInfo addAgentInfo(ComplexAgentInfo info) {
		agentInfoVector.add(info);
		return info;
	}

	public int getInfoNum() {
		return agentInfoVector.size();
	}

	public void resetAgentInfo() {
		agentInfoVector = new Vector<ComplexAgentInfo>();
	}

	/**
	 * Dump header for report file to path
	 * @param filepath path to report file
	 */
	public void printAgentHeader(java.io.PrintWriter pw) {
		try {
		    StringBuffer buffer = new StringBuffer();

			String agentInfoHeader = "Agent Number";
			agentInfoHeader += "\tAgent Type";
			agentInfoHeader += "\tBirth Tick";
			agentInfoHeader += "\tBirth Type";
			agentInfoHeader += "\tDeath Tick";
			agentInfoHeader += "\tGenetic Code";
			agentInfoHeader += "\tDirect Descendants";
			agentInfoHeader += "\tTotal Descendants";
			agentInfoHeader += "\tSexual Pregnancies";
			agentInfoHeader += "\tSteps";
			agentInfoHeader += "\tTurns";
			agentInfoHeader += "\tAgent Bumps";
			agentInfoHeader += "\tRock Bumps";
			agentInfoHeader += "\tStrategy";
			buffer.append(agentInfoHeader);
			buffer.append("\n");
			pw.write(buffer.toString());
		}
		catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	public void printAgentInfo(java.io.PrintWriter pw) {
		ComplexAgentInfo.initStaticAgentInfo(agentTypeCount);

		// Concatenating the headers of the report file.
		String agentInfoHeader = "Agent Number";
		agentInfoHeader += "\tAgent Type";
		agentInfoHeader += "\tBirth Tick";
		agentInfoHeader += "\tBirth Type";
		agentInfoHeader += "\tDeath Tick";
		agentInfoHeader += "\tGenome";
		agentInfoHeader += "\tRed Gene Value";
		agentInfoHeader += "\tGreen Gene Value";
		agentInfoHeader += "\tBlue Gene Value";
		agentInfoHeader += "\tRed Gene Status";
		agentInfoHeader += "\tGreen Gene Status";
		agentInfoHeader += "\tBlue Gene Status";
		agentInfoHeader += "\tDirect Descendants";
		agentInfoHeader += "\tTotal Descendants";
		agentInfoHeader += "\tSexual Pregnancies";
		agentInfoHeader += "\tSteps";
		agentInfoHeader += "\tTurns";
		agentInfoHeader += "\tAgent Bumps";
		agentInfoHeader += "\tRock Bumps";
		agentInfoHeader += "\tStrategy";

		pw.println(agentInfoHeader);

		pw.println(); // Type info follows
		String agentTypeHeaders = "Agent Type";
		agentTypeHeaders += "\tDeaths";
		agentTypeHeaders += "\tLiving";
		agentTypeHeaders += "\tTotal Offsprings";
		agentTypeHeaders += "\tAsexual Births";
		agentTypeHeaders += "\tSexual Births";
		agentTypeHeaders += "\tSteps";
		agentTypeHeaders += "\tAverage Red Gene Status";
		agentTypeHeaders += "\tAverage Green Gene Status";
		agentTypeHeaders += "\tAverage Blue Gene Status";

		pw.println(agentTypeHeaders);

		// Prints out species-wise statistics of each agent type
		for (int i = 0; i < AGENT_TYPES; i++) {
			ComplexAgentInfo.printAgentsCountByType(pw,i); // Steps, deaths, births, etc.
			pw.print("\n");
		}
	}

	/*
	 * Write to Log file: FoodCount, AgentCount, Average Agent Energy and Agent
	 * Energy at the most recent ticks ( by tick and by Agent/Food preference)
	 */
	@Override
	public void writeLogEntry() {
		if (logStream == null) {
			return;
		}

		java.text.DecimalFormat z = new DecimalFormat("#,##0.000");
		double[][] avg_gene_status = getAvgGeneStatus();

		setTickCount(theScheduler.getTime()); // $$$$$$ get the current tick.  Apr 19
		// For this tick: print FoodCount, AgentCount, Average Agent Energy
		// and Agent Energy for EACH AGENT TYPE
		for (int i = 0; i < agentTypeCount; i++) {

			long agentCount = countAgents(i);
			long agentEnergy = countAgentEnergy(i);
			/*System.out
					.println("************* Near Agent Count *************");*/

			int cheaters = (numAgentsStrat(i))[1];
			int coops = (numAgentsStrat(i))[0];
			logStream.print(tickCount);
			logStream.print('\t');
			logStream.print(countFoodTiles(i));
			logStream.print('\t');
			logStream.print(agentCount);
			logStream.print('\t');
			// Format Average agentEnergy to 3 decimal places
			if (agentCount != 0)
				logStream.print(z
						.format(((float) agentEnergy) / agentCount));
			else
			logStream.print("00.000");
			logStream.print('\t');
			logStream.print(agentEnergy);
			logStream.print('\t');
			logStream.print(cheaters);
			logStream.print('\t');
			logStream.print(coops);
			logStream.print('\t');
			for (int j = 0; j < GeneticCode.NUM_GENES; j++) {
				logStream.print(avg_gene_status[i][j]);
				logStream.print('\t');
			}
			// logStream.flush();
		}
		// print the TOTAL of FoodCount, AgentCount, Average Agent Energy
		// and Agent Energy at a certain tick
		/*System.out
				.println("************* Before Agent Count Call *************");*/
		long agentCountAll = countAgents();
		/*System.out
				.println("************* After Agent Count Call *************");*/
		long agentEnergyAll = countAgentEnergy();
		int total_cheaters = (numAgentsStrat())[1];
		int total_coops = (numAgentsStrat())[0];
		logStream.print(tickCount);
		logStream.print('\t');
		logStream.print(countFoodTiles());
		logStream.print('\t');
		logStream.print(agentCountAll);
		logStream.print('\t');
		logStream.print(z.format(((float) agentEnergyAll) / agentCountAll));
		logStream.print('\t');
		logStream.print(agentEnergyAll);
		logStream.print('\t');
		logStream.print(total_cheaters);
		logStream.print('\t');
		logStream.print(total_coops);
		logStream.println('\t');
		//logStream.flush();
	}

	/* Return the number of agents (int) for a certain agentType (int) */
	private int countAgents(int agentType) {
		int agentCount = 0;
		java.util.Enumeration<cobweb.Agent> e = getAgents();
		while (e.hasMoreElements()) {
			ComplexAgent agent = (ComplexAgent) e.nextElement();
			if (agent.getAgentType() == agentType)
				agentCount++;
		}
		return agentCount;
	}

	// Hidden implementation stuff...

	// Bitmasks for boolean states
	private static final long MASK_TYPE = 15;

	private static final long STONE_CODE = 1;

	private static final long FOOD_CODE = 2;

	private static final long WASTE_CODE = 4;

	@Override
	protected boolean testFlag(cobweb.Environment.Location l, int flag) {
		switch (flag) {
		case FLAG_STONE:
			return ((getLocationBits(l) & MASK_TYPE) == STONE_CODE);
		case FLAG_FOOD:
			return ((getLocationBits(l) & MASK_TYPE) == FOOD_CODE);
		case FLAG_WASTE:
			return ((getLocationBits(l) & MASK_TYPE) == WASTE_CODE);
		default:
			return false;
		}
	}

	/*
	 * setFlag flags the location as a food/stone location. It does nothing if
	 * the square is already occupied (for example, setFlag((0,0),FOOD,true)
	 * does nothing when (0,0) is a stone
	 */
	@Override
	protected void setFlag(cobweb.Environment.Location l, int flag,
			boolean state) {
		switch (flag) {
		case FLAG_STONE:
			// Sanity check
			if ((getLocationBits(l) & (FOOD_CODE | WASTE_CODE)) != 0)
				break;
			if (state) {
				setLocationBits(l, (getLocationBits(l) & ~MASK_TYPE)
						| STONE_CODE);
			} else {
				setLocationBits(l, getLocationBits(l) & ~MASK_TYPE);
			}
			break;
		case FLAG_FOOD:
			// Sanity check
			if ((getLocationBits(l) & (STONE_CODE | WASTE_CODE)) != 0)
				break;
			if (state) {
				setLocationBits(l, (getLocationBits(l) & ~MASK_TYPE)
						| FOOD_CODE);
			} else
				setLocationBits(l, getLocationBits(l) & ~MASK_TYPE);
			break;
		case FLAG_WASTE:
			// Sanity check
			if ((getLocationBits(l) & (FOOD_CODE | STONE_CODE)) != 0)
				break;
			if (state) {
				setLocationBits(l, (getLocationBits(l) & ~MASK_TYPE)
						| WASTE_CODE);
			} else {
				setLocationBits(l, getLocationBits(l) & ~MASK_TYPE);
			}
			break;
		default:
		}
	}

	@Override
	public int getTypeCount() {

		return agentTypeCount;
	}

	// Ignored; this model has no fields
	@Override
	protected int getField(cobweb.Environment.Location l, int field) {
		return 0;
	}

	@Override
	protected void setField(cobweb.Environment.Location l, int field, int value) {
	}

	@Override
	public int getAxisCount() {
		return 2;
	}

	@Override
	public int getSize(int axis) {
		return array.getSize(axis);
	}

	@Override
	public boolean getAxisWrap(int axis) {
		return wrapMap[0];
	}

	protected long getLocationBits(cobweb.Environment.Location l) {
		return array.getLocationBits(l);
	}

	protected void setLocationBits(cobweb.Environment.Location l, long bits) {
		array.setLocationBits(l, bits);
	}

	private java.io.PrintWriter logStream;

	@SuppressWarnings("unused")
	private java.io.PrintWriter trackAgentStream;

	private Vector<ComplexAgentInfo> agentInfoVector = new Vector<ComplexAgentInfo>();

	private cobweb.ArrayEnvironment array;

	/*
	 * Waste tile array to store the data per waste tile. Needed to allow
	 * depletion of waste
	 */
	private static Waste[][] wastearray;

	public static final double E = 2.7182818284590452353602875;

	static class Waste {
		private int initialWeight;

		private long birthTick;

		private float rate;

		private double threshold;

		private boolean valid;

		@SuppressWarnings("unused")
		private long lastAmount = 0;

		public Waste(long birthTick, int weight, float rate) {
			initialWeight = weight;
			this.birthTick = birthTick;
			this.rate = rate;
			// to avoid recalculating every tick
			threshold = 0.001 * initialWeight; // changed from 0.5 to 0.001 by
												// skinawy
			this.valid = true;
		}

		public boolean isActive(long tick) {
			if (!valid)
				return false;
			if (getAmount(tick) < threshold) {
				valid = false;
				return false;
			}
			return true;
		}

		/* Call me sparsely, especially when the age is really old */
		public double getAmount(long tick) {
			return initialWeight * Math.pow(E, -1 * rate * (tick - birthTick));
		}

		public void reset(long birthTick, int weight, float rate) {
			initialWeight = weight;
			this.birthTick = birthTick;
			this.rate = rate;
			// to avoid recalculating every tick
			threshold = 0.001 * initialWeight; // changed from 0.5 to 0.001 by
												// skinawy
			valid = true;
		}
	}

	public static void addWaste(long tick, int x, int y, int val, float rate) {
		if (wastearray[x][y] != null)
			wastearray[x][y].reset(tick, val, rate);
		else
			wastearray[x][y] = new Waste(tick, val, rate);
	}

	/* //[]SK */

	public static class CommPacket {

		private static final int DEFAULT = 1;

		public static final int FOOD = 1;

		public static final int CHEATER = 2;

		CommManager commManager = new CommManager();

		/**
		 * Constructor
		 *
		 * @return void
		 *
		 */
		public CommPacket(int type, long dispatcherId, String content,
				int energy, boolean energyBased, int fixedRange) {

			this.packetId = ++packetCounter;
			this.type = type;
			this.dispatcherId = dispatcherId;
			this.content = content; // could be a message or an enumerated type
									// depending on the type
			if (!energyBased)
				this.radius = getRadius(energy);
			else
				this.radius = fixedRange;
			this.persistence = DEFAULT;

			// CommManager commManager = new CommManager();
			commManager.addPacketToList(this);
		}

		private static int packetCounter;

		private int packetId; // Unique ID for each communication packet

		private int type; // Type of packet (Food or ). This is an enumerated
							// number that can be extended

		private long dispatcherId; // ID of sending agent (or entity)...could
									// be modified to take an Object type

		// According to this sender ID, other members may decide whether or not
		// to accept this message
		private String content; // Content of message. e.g. Cheater with ID 1234
								// encountered...migth be enumerated

		// e.g. Food found at location 34,43
		private int radius; // Reach over the whole environment or just a
							// certain neighborhood

		private int persistence; // how many time steps should the packet
									// stay there. By default, a packet will
									// persist for one time step (value 1)

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public long getDispatcherId() {
			return dispatcherId;
		}

		public void setDispatcherId(int dispatcherId) {
			this.dispatcherId = dispatcherId;
		}

		public int getPacketId() {
			return packetId;
		}

		public void setPacketId(int packetId) {
			this.packetId = packetId;
		}

		public int getPersistence() {
			return persistence;
		}

		public void setPersistence(int persistence) {
			this.persistence = persistence;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		private int getRadius(int energy) {
			return energy / 10 + 1; // limiting minimum to 1 unit of
											// radius
		}

		public int getRadius() {
			return radius;
		}

		public void setRadius(int radius) {
			this.radius = radius;
		}
	}

	public static class CommManager {

		private boolean broadcastBlocked = false;

		/**
		 * adds packets to the list of packets
		 *
		 * @param packet
		 * @return void
		 */
		public void addPacketToList(CommPacket packet) {
			if (!broadcastBlocked)
				currentPackets.add(packet);
			blockBroadcast();
		}

		/**
		 * removes packets from the list of packets
		 *
		 * @param packet
		 * @return void
		 */
		public void removePacketfromList(CommPacket packet /* int packetId */) {
			currentPackets.remove(/* getPacket(packetId) */packet);
		}

		// with every time step, the persistence of the packets should be
		// decremented
		public void decrementPersistence() {
			int pValue;
			for (int i = 0; i < currentPackets.size(); i++) {
				pValue = --currentPackets.get(i).persistence;
				if (pValue <= 0)
					removePacketfromList(currentPackets.get(i));
			}
		}

		public boolean packetInRange(int range,
				cobweb.Environment.Location broadcastPos,
				cobweb.Environment.Location position) {
			if (Math.abs(position.v[0] - broadcastPos.v[0]) > range)
				return false;
			if (Math.abs(position.v[1] - broadcastPos.v[1]) > range)
				return false;
			return true;
		}

		/**
		 * returns the packet with the specified ID
		 *
		 * @return CommPacket
		 */
		public CommPacket getPacket(int packetId) {
			int i = 0;
			while (packetId != (currentPackets.get(i)).packetId
					& i < currentPackets.size()) {
				i++;
			}
			return currentPackets.get(i);
		}

		public void blockBroadcast() {
			broadcastBlocked = true;
		}

		public void unblockBroadcast() {
			broadcastBlocked = false;
		}

	}

	private int[] draughtdays = new int[10];

	// Food array contains type and their locations
	private static int[][] foodarray = new int[0][];

	@SuppressWarnings("unused")
	private int clickcount = 0;

	// Returns current location's food type
	public static int getFoodType(cobweb.Environment.Location l) {
		return foodarray[l.v[0]][l.v[1]];
	}

	// Sets Food Type in foodarray [];
	public void setFoodType(cobweb.Environment.Location l, int i) {
		foodarray[l.v[0]][l.v[1]] = i;
	}

	private Agent observedAgent = null;

	/*
	 * This gets called when the user clicks on a tile without selecting outside
	 * of edit mode
	 */
	@Override
	public void observe(int x, int y, cobweb.UIInterface ui) {
		cobweb.Environment.Location l = getUserDefinedLocation(x, y);
		/* A tile can only consist of: STONE or WASTE or (FOOD|AGENT) */
		observedAgent = l.getAgent();
	}

	@Override
	public EnvironmentStats getStatistics() {
		EnvironmentStats stats = new EnvironmentStats();
		stats.agentCounts = new long[getAgentTypes()];
		stats.foodCounts = new long[getAgentTypes()];
		for (int i = 0; i < getAgentTypes(); i++) {
			stats.agentCounts[i] = countAgents(i);
			stats.foodCounts[i] = countFoodTiles(i);
		}
		stats.timestep = this.getTickCount();
		return stats;
	}

	@Override
	protected synchronized void getDrawInfo(UIInterface theUI) {
		super.getDrawInfo(theUI);
		for (Agent a : agentTable.values()) {
			a.getDrawInfo(theUI);
		}

		if (observedAgent != null) {
			theUI.newPath(((ComplexAgent)observedAgent).getInfo().getPathHistory());
		}

	}

}
