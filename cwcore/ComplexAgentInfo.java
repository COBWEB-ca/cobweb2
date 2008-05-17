/*  $$$$$:  Comments by Liang
 * 
 *  $$$$$$: Codes modified and/or added by Liang
 */

package cwcore;

import java.text.DecimalFormat;
import java.io.*;
import ga.GeneticCode;
import ga.GeneticCodeException;

public class ComplexAgentInfo {

	/** Stores the genetic code. */
	private String genes;

	/** The gene status of the agent. Default is 1. */
  
	private double[] gene_status = {1,1,1};
  
	/** Returns the status of each gene. */
  	public double[] getGeneStatus() {
  		return gene_status;
  	}
	
	public ComplexAgentInfo(int num, int type, long birth, int strat) {
		agentType = type;
		agentNumber = num;
		birthTick = birth;
		action = strat;
	}

	public ComplexAgentInfo(int num, int type, long birth, ComplexAgentInfo p1,
			ComplexAgentInfo p2, int strat) {
		agentType = type;
		agentNumber = num;
		birthTick = birth;
		parent1 = p1;
		parent2 = p2;
		action = strat;
	}

	public ComplexAgentInfo(int num, int type, long birth, ComplexAgentInfo p1,
			int strat) {
		agentType = type;
		agentNumber = num;
		birthTick = birth;
		parent1 = p1;
		action = strat;
	}

	public void addDirectChild() {
		++directChildren;
	}

	public void addStep() {
		++countSteps;
	}

	public void addTurn() {
		++countTurns;
	}

	public void addAgentBump() {
		++countAgentBumps;
	}

	public void addRockBump() {
		++countRockBumps;
	}

	public void addSexPreg() {
		++sexualPregs;
	}

	public void ate(int agentType) {
		eatenAgentsofType[agentType]++;
	}

	/**
	 * This method should be called prior to calling the printInfo method. This
	 * method will initializes the static array. Should be called once not in a
	 * loop.
	 */

	public static void initStaticAgentInfo(int agenttypes) {
		if (alreadyInitialized) {
			System.out.println("ComplexAgentInfo::initStaticAgentInfo("
					+ agenttypes + ")"
					+ " ComplexAgentInfo::alreadyInitialized = "
					+ alreadyInitialized);
			return;
		}
		System.out.println("ComplexAgentInfo::initStaticAgentInfo("
				+ agenttypes + ")" + " ComplexAgentInfo::alreadyInitialized = "
				+ alreadyInitialized);
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

	public static void resetGroupData() {
		for (int i = 0; i < MAX_NUM_OF_AGENTS; i++) {
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

	/* Formatting variables. The WIDTH of certain columns */
	final static int W_TICK = 6;

	final static int W_ENERGY = 8;

	final static int W_MOVE = 6;

	final static int W_AGENT = 6;

	final static int W_ROCK = 6;

	final static int W_OTHER_GAIN = 6;

	final static int W_OTHER_LOSS = 6;

	final static int W_CANN = 6;

	final static int W_TURNING = 6;

	final static int W_SPACE = 2;

	final static int W_POP = 6;

	final static int W_FEED = 6;

	final static int W_AGE = 6;

	private static boolean headerPrinted = false;

	private static PrintWriter group_out;

	private static boolean groupDataStreamInit = false;

	/* Dumps the group data */
	public static void dumpGroupData(long tick, java.io.Writer www) {
		if (!groupDataStreamInit) {
			try {
				group_out = new java.io.PrintWriter(www, true);
				groupDataStreamInit = true;
			} catch (Exception e) {
			}
		} else {
			try {
				int agentTypes = 4;
				if (!headerPrinted) {
					group_out.print(paddMe("", W_TICK));
					// TITLE LINE 1
					for (int i = 0; i < agentTypes; i++) {
						group_out.print("||");
						group_out.print(paddMe("Type " + (i + 1), W_POP));
						group_out.print(paddMe("gained", W_FEED));
						group_out.print(paddMe("gained", W_CANN));
						group_out.print(paddMe("gained", W_OTHER_GAIN));
						group_out.print(paddMe("energy", W_ENERGY));
						group_out.print(paddMe("usage", W_MOVE));
						group_out.print(paddMe("usage", W_AGENT));
						group_out.print(paddMe("usage", W_ROCK));
						group_out.print(paddMe("usage", W_TURNING));
						group_out.print(paddMe("usage", W_OTHER_LOSS));
						group_out.print(paddMe("usage", W_AGE));
					}
					group_out.println();
					// TITLE LINE 2
					group_out.print(paddMe("TICK", W_TICK));
					for (int i = 0; i < agentTypes; i++) {
						group_out.print("||");
						group_out.print(paddMe("pop.", W_POP));
						group_out.print(paddMe("food", W_FEED));
						group_out.print(paddMe("cann", W_CANN));
						group_out.print(paddMe("others", W_OTHER_GAIN));
						group_out.print(paddMe("total", W_ENERGY));
						group_out.print(paddMe("moving", W_MOVE));
						group_out.print(paddMe("agents", W_AGENT));
						group_out.print(paddMe("rocks", W_ROCK));
						group_out.print(paddMe("turn", W_TURNING));
						group_out.print(paddMe("others", W_OTHER_LOSS));
						group_out.print(paddMe("age", W_AGE));
					}
					group_out.println();
					headerPrinted = true;
				} // !headerPrinted
				group_out.print(paddMe(tick, W_TICK));

				// DATA
				for (int i = 0; i < agentTypes; i++) {
					group_out.print("||");
					group_out.print(paddMe(liveCount[i], W_POP));
					group_out.print(paddMe(foodEnergies[i], W_FEED));
					group_out.print(paddMe(cannibalEnergies[i], W_CANN));
					group_out
							.print(paddMe(otherEnergySources[i], W_OTHER_GAIN));
					group_out.print(paddMe(energies[i], W_ENERGY));
					group_out.print(paddMe(stepEnergies[i], W_MOVE));
					group_out.print(paddMe(agentBumpEnergies[i], W_AGENT));
					group_out.print(paddMe(rockBumpEnergies[i], W_ROCK));
					group_out.print(paddMe(turningEnergies[i], W_TURNING));
					group_out.print(paddMe(otherEnergySinks[i], W_OTHER_LOSS));
					group_out.print(paddMe(agentAgingEnergies[i], W_AGE));
				}
				group_out.println();
				group_out.flush();
			} catch (Exception e) {
				System.err.println("dumpGroupInfo failure: " + e);
			}
		}
	}

	public void printInfo(java.io.PrintWriter pw) {
		pw.print(agentNumber);
		pw.print("\t" + (agentType + 1));  // $$$$$$ change from "agentType" to "(agentType + 1)".  Apr 3
		pw.print("\t" + birthTick);
		if (parent1 == null && parent2 == null) {
			asexAgentsofType[agentType]++;
			pw.print("\tRandomly generated");
		} else if (parent2 == null) {
			asexAgentsofType[agentType]++;
			offspringsAgentsofType[parent1.agentType]++;
			pw.print("\tAsexual, from agent " + parent1.agentNumber);
		} else {
			pw.print("\tSexual, from Mother: " + parent1.agentNumber
					+ ", Father: " + parent2.agentNumber);
			sexAgentsofType[agentType]++;
			offspringsAgentsofType[parent1.agentType]++;
			offspringsAgentsofType[parent2.agentType]++;
		}
		pw.print("\t" + deathTick);
		if (deathTick != -1) {
			stepslivedAgentsofType[agentType] += (deathTick - birthTick);
			deadAgentsofType[agentType]++;
		} else {
			aliveAgentsofType[agentType]++;
		}
		
		// $$$$$$ The following line is added to format the genetic code output for correctly reporting and showing by MS Excel.  Feb 5
		String genesString = genes.substring(0, 8) + " " + genes.substring(8, 16) +  " " + genes.substring(16, 24);
		pw.print("\t" + genesString); // $$$$$$ Pass the reformed genetic code "geensString" instead of "genes".  Feb 5
		
		// Output the values of each gene and their status
		try {
			pw.print("\t" + GeneticCode.byteToInt(genes.substring(0, 8)));
			pw.print("\t" + GeneticCode.byteToInt(genes.substring(8, 16)));
			pw.print("\t" + GeneticCode.byteToInt(genes.substring(16, 24)));
			pw.print("\t" + gene_status[0]);
			pw.print("\t" + gene_status[1]);
			pw.print("\t" + gene_status[2]);
		} catch (GeneticCodeException e) {
			System.err.println("ComplexAgeintInfo: Invalid gene format.");
		}
		pw.print("\t" + directChildren);
		pw.print("\t" + totalChildren);
		pw.print("\t" + sexualPregs);

		int total = countSteps + countTurns + countAgentBumps + countRockBumps;
		totalsAgentsofType[agentType] += total;
		stepsAgentsofType[agentType] += countSteps;
		DecimalFormat dform = new DecimalFormat("###.##%");
		pw.print("\t" + dform.format((double) (countSteps) / total));
		pw.print("\t" + dform.format((double) (countTurns) / total));
		pw.print("\t" + dform.format((double) (countAgentBumps) / total));
		pw.print("\t" + dform.format((double) (countRockBumps) / total));

		if (action == 0) {
			pw.println("\tcooperator");
		} else {
			pw.println("\tcheater");
		}
	}

	/******* Defunct */
	public static void printAgentsCount(java.io.PrintWriter pw) {
		for (int i = 0; i < agentTypes; i++) {
			pw.print((i + 1)); // $$$$$$ change from "i" to "(i + 1)".  Apr 3
			pw.print("\t" + deadAgentsofType[i]);
			pw.print("\t" + aliveAgentsofType[i]);
			pw.print("\t" + offspringsAgentsofType[i]);
			pw.print("\t" + sexAgentsofType[i]);
			pw.print("\t" + asexAgentsofType[i]);
			pw.println("\t" + stepsAgentsofType[i]);
		}
	}
	
	/** Prints the species-wise statistics of an agent type. Intended to replace printAgentsCount() */
	public static void printAgentsCountByType(java.io.PrintWriter pw, int type) {
		pw.print((type + 1)); // $$$$$$ change from "i" to "(i + 1)".  Apr 3
		pw.print("\t" + deadAgentsofType[type]);
		pw.print("\t" + aliveAgentsofType[type]);
		pw.print("\t" + offspringsAgentsofType[type]);
		pw.print("\t" + sexAgentsofType[type]);
		pw.print("\t" + asexAgentsofType[type]);
		pw.print("\t" + stepsAgentsofType[type]);
	}

	public void setDeath(long death) {
		deathTick = death;
	}
	
	public long getDeathTick() {
		return deathTick;
	}

	public int getTypeNum() {
		return agentTypes;

	}

	public int getAgentNumber() {
		return agentNumber;
	}
	
	public void setStrategy(int strat) {
		action = strat;
	}

	/** Sets the genetic code and gene stati. */
	public void setGeneticCode(String geneticCode, int[] gene_values) {
		genes = geneticCode;		  
	    for (int i = 0; i < GeneticCode.NUM_GENES; i++) {
	    	gene_status[i] = 2*Math.abs(Math.sin(gene_values[i]*Math.PI/180));
	    }
	}


	// The static variables to keep track of the agents produced so far.

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

	private int action = 0;

	private ComplexAgentInfo parent1;

	private ComplexAgentInfo parent2;

	private long birthTick;

	private long deathTick = -1;

	private int countSteps;

	private int countTurns;

	private int countAgentBumps;

	private int countRockBumps;

	private int agentNumber;

	private int agentType;
	
	public int getAgentType() {
		return agentType;
	}

	private int sexualPregs;

	private int directChildren;

	private int totalChildren;

	private static boolean alreadyInitialized = false;

	@SuppressWarnings("unused")
	private static int[] cheatingAgent;

	@SuppressWarnings("unused")
	private static int[] cooperatingAgent;

	private static int MAX_NUM_OF_AGENTS = 4;

	private static int[] energies = new int[MAX_NUM_OF_AGENTS];

	private static int[] stepEnergies = new int[MAX_NUM_OF_AGENTS];

	private static int[] agentBumpEnergies = new int[MAX_NUM_OF_AGENTS];

	private static int[] rockBumpEnergies = new int[MAX_NUM_OF_AGENTS];

	private static int[] liveCount = new int[MAX_NUM_OF_AGENTS];

	private static int[] foodEnergies = new int[MAX_NUM_OF_AGENTS];

	private static int[] otherEnergySources = new int[MAX_NUM_OF_AGENTS];

	private static int[] cannibalEnergies = new int[MAX_NUM_OF_AGENTS];

	private static int[] turningEnergies = new int[MAX_NUM_OF_AGENTS];

	private static int[] otherEnergySinks = new int[MAX_NUM_OF_AGENTS];

	private static int[] agentAgingEnergies = new int[MAX_NUM_OF_AGENTS];

	/* Total energy per agent type */
	public void addEnergy(int type, int val) {
		energies[type] += val;
	}

	/* addXXX == energy gained from XXX */
	public void addFoodEnergy(int type, int val) {
		foodEnergies[type] += val;
	}

	/* Other sources including energy gained from the agent strategy */
	public void addOthers(int type, int val) {
		otherEnergySources[type] += val;
	}

	public void addCannibalism(int type, int val) {
		cannibalEnergies[type] += val;
	}

	/* useXXX == energy consumed for XXX */
	public void useAgentBumpEnergy(int type, int val) {
		agentBumpEnergies[type] += val;
	}

	public void useStepEnergy(int type, int val) {
		stepEnergies[type] += val;
	}

	public void useRockBumpEnergy(int type, int val) {
		rockBumpEnergies[type] += val;
	}

	public void useTurning(int type, int val) {
		turningEnergies[type] += val;
	}

	/* Other sources including energy gained from the agent strategy */
	public void useOthers(int type, int val) {
		otherEnergySinks[type] += val;
	}

	public void useExtraEnergy(int type, int val) {
		agentAgingEnergies[type] += val;
	}

	/* Keep a total tally of the living agents */
	public void alive(int type) {
		liveCount[type]++;
	}

}
