package ga;

import cwcore.ComplexAgent;
import cwcore.ComplexEnvironment;


public class GATracker {
	
	/** The size of distribution of gene status. 91 given our |2sin(x)| function */
	public static final int GENE_STATUS_DISTRIBUTION_SIZE = 91;
	
	/** The size of distribution of gene valu. 256 given our 8-bit per gene system */
	public static final int GENE_VALUE_DISTRIBUTION_SIZE = 256;	
	
	/** Number of agents stored in GATracker's 'agents' */
	public static int[] total_agents = new int[ComplexEnvironment.AGENT_TYPES];
	
	/** The sum of specific gene status numbers over particular agent types. */ 
	public static double[][] total_gene_status = new double[ComplexEnvironment.AGENT_TYPES][GeneticCode.NUM_GENES];
	
	/** The distribution of the status of a specific gene over particular agent types. */
	private static double[][][] gene_status_distribution = new double [ComplexEnvironment.AGENT_TYPES][GeneticCode.NUM_GENES][GENE_STATUS_DISTRIBUTION_SIZE];
	
	/** The distribution of the value of a specific gene over particular agent types. */
	private static double[][][] gene_value_distribution = new double [ComplexEnvironment.AGENT_TYPES][GeneticCode.NUM_GENES][GENE_VALUE_DISTRIBUTION_SIZE];
	
	/** Which GA-info type to track/print. */
	private static boolean track_gene_status_distribution = false;
	private static boolean track_gene_value_distribution = false;
	
	/** The seed used by the program. */
	public static long seed;
	
	/** Adds an agent. */
	public static void addAgent(ComplexAgent ca) {
		for (int i = 0; i < GeneticCode.NUM_GENES; i++) {
			double gene_status = ca.getInfo().getGeneStatus()[i];
			int gene_value = ca.getGeneticCode().getGeneticColour()[i];
			total_gene_status[ca.getInfo().getAgentType()][i] += gene_status;
			gene_status_distribution[ca.getInfo().getAgentType()][i][geneStatusHash(gene_value)]++;
			gene_value_distribution[ca.getInfo().getAgentType()][i][gene_value]++;
		}
		total_agents[ca.getInfo().getAgentType()]++;

	}
	
	/** Removes an agent. */
	public static void removeAgent(ComplexAgent ca) {
		for (int i = 0; i < GeneticCode.NUM_GENES; i++) {
			double gene_status = ca.getInfo().getGeneStatus()[i];
			int gene_value = ca.getGeneticCode().getGeneticColour()[i];
			total_gene_status[ca.getInfo().getAgentType()][i] -= gene_status;
			gene_status_distribution[ca.getInfo().getAgentType()][i][geneStatusHash(gene_value)]--;
			gene_value_distribution[ca.getInfo().getAgentType()][i][gene_value]--;
		}
		total_agents[ca.getInfo().getAgentType()]--;
	}
	
	/** Calculates GA info and prints them if appropriate. */
	public static void printGAInfo(long time_step) {			
		if (track_gene_status_distribution) {
			plotGeneStatusDistribution(time_step);			
		}
		
		if (track_gene_value_distribution) {
			plotGeneValueDistribution(time_step);			
		}
	}

	/** Plot the gene status distribution of all agent types for a certain time step. */
	private static void plotGeneStatusDistribution(long time_step) {
		// Update the chart according to update frequency
		if (time_step % GAChartOutput.update_frequency == 0) { 
			GAChartOutput.updateGeneStatusDistributionData(gene_status_distribution);
		}
	}
	
	/** Plot the gene value distribution of all agent types for a certain time step. */
	private static void plotGeneValueDistribution(long time_step) {
		// Update the chart according to update frequency
		if (time_step % GAChartOutput.update_frequency == 0) { 
			GAChartOutput.updateGeneValueDistributionData(gene_value_distribution);
		}
	}
	
	/** Gets the appropriate index of a gene of a specific value in a gene status distribution hash table (or array). */	
	public static int geneStatusHash(int gene_value) {
		int index;
		if (gene_value > 90) {
			index = Math.abs(180 - gene_value);
		} else {
			index = gene_value;
		}
		return index;
	}		
	
	/** Changes the current state of 'track_gene_status_distribution' and return the altered state. */ 
	public static boolean negateTrackGeneStatusDistribution() {
		if (track_gene_status_distribution) {
			track_gene_status_distribution = false;
		} else {
			track_gene_status_distribution = true;			
		}
		return track_gene_status_distribution;
	}
	
	/** Returns the current state of 'track_gene_status_distribution'. */
	public static boolean getTrackGeneStatusDistribution() {
		return track_gene_status_distribution;
	}
	
	/** Sets the state of 'track_gene_status_distribution'. */
	public static void setTrackGeneStatusDistribution(boolean state) {
		track_gene_status_distribution = state;
	}
	
	/** Changes the current state of 'track_gene_value_distribution' and return the altered state. */ 
	public static boolean negateTrackGeneValueDistribution() {
		if (track_gene_value_distribution) {
			track_gene_value_distribution = false;
		} else {
			track_gene_value_distribution = true;			
		}
		return track_gene_value_distribution;
	}
	
	
	/** Returns the current state of 'track_gene_value_distribution'. */
	public static boolean getTrackGeneValueDistribution() {
		return track_gene_value_distribution;
	}
	
	/** Sets the state of 'track_gene_value_distribution'. */
	public static void setTrackGeneValueDistribution(boolean state) {
		track_gene_value_distribution = state;
	}
	
	/** Initialize the output of GA info. i.e. Setting up output streams and naming headers in files */	
	public static void initializeGAInfoOutput() {
		GAChartOutput gco = new GAChartOutput();
		gco.initPlots();	
		if (track_gene_status_distribution) {
			// Initialize chart output
			GAChartOutput.updateGeneStatusDistributionData(gene_status_distribution);
		}
		
		if (track_gene_value_distribution) {
			// Initialize chart output	
			GAChartOutput.updateGeneValueDistributionData(gene_value_distribution);
		}
	}

}
