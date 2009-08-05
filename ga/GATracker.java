package ga;

import cobweb.ArrayUtilities;
import cobweb.TickScheduler.Client;


public class GATracker implements Client {



	/** The size of distribution of gene status. 91 given our |2sin(x)| function */
	public static final int GENE_STATUS_DISTRIBUTION_SIZE = 91;

	/** The size of distribution of gene valu. 256 given our 8-bit per gene system */
	public static final int GENE_VALUE_DISTRIBUTION_SIZE = 256;

	/** Number of agents stored in GATracker's 'agents' */
	public int[] total_agents;

	/** The sum of specific gene status numbers over particular agent types. */
	public double[][] total_gene_status;

	/** The distribution of the status of a specific gene over particular agent types. */
	private double[][][] gene_status_distribution;

	/** The distribution of the value of a specific gene over particular agent types. */
	private double[][][] gene_value_distribution;

	/** Which GA-info type to track/print. */
	private boolean track_gene_value_distribution;

	private GAChartOutput charOutput;

	private int typeCount;

	private int geneCount;

	private int update_frequency;

	public GATracker() {
		// Nothing
	}

	public void setParams(int agentTypes, int geneNo, boolean track, int update_frequency, String[] names) {
		typeCount = agentTypes;
		geneCount = geneNo;
		this.update_frequency = update_frequency;
		frameskip = 0;

		if (total_agents == null) {
			total_agents = new int[typeCount];
		} else if (total_agents.length != typeCount) {
			total_agents = ArrayUtilities.resizeArray(total_agents, typeCount);
		}

		if (total_gene_status == null) {
			total_gene_status = new double[typeCount][geneCount];
		} else if (total_gene_status.length != typeCount || total_gene_status[0].length != geneCount) {
			total_gene_status = ArrayUtilities.resizeArray(total_gene_status, typeCount, geneCount);
		}

		if (gene_status_distribution == null) {
			gene_status_distribution = new double [typeCount][geneCount][GENE_STATUS_DISTRIBUTION_SIZE];
		} else if (gene_status_distribution.length != typeCount || gene_status_distribution[0].length != geneCount) {
			gene_status_distribution = ArrayUtilities.resizeArray(gene_status_distribution, typeCount, geneCount, GENE_STATUS_DISTRIBUTION_SIZE);
		}

		if (gene_value_distribution == null) {
			gene_value_distribution =  new double [typeCount][geneCount][GENE_VALUE_DISTRIBUTION_SIZE];
		} else if (gene_value_distribution.length != typeCount || gene_value_distribution[0].length != geneCount) {
			gene_value_distribution = ArrayUtilities.resizeArray(gene_value_distribution, typeCount, geneCount, GENE_VALUE_DISTRIBUTION_SIZE);
		}
		track_gene_value_distribution = track;

		if (track_gene_value_distribution) {
			charOutput = new GAChartOutput(typeCount, geneCount, names);
			// Initialize chart output
			charOutput.updateGeneStatusDistributionData(gene_value_distribution, gene_status_distribution);
			plotGeneValueDistribution(0);
		}
		frameskip = 0;
	}

	/** Adds an agent. */
	public void addAgent(int type, GeneticCode genes) {
		for (int i = 0; i < geneCount; i++) {
			total_gene_status[type][i] += genes.getStatus(i);
			gene_status_distribution[type][i][geneStatusHash(genes.getValue(i))]++;
			gene_value_distribution[type][i][genes.getValue(i)]++;
		}
		total_agents[type]++;
	}

	/** Removes an agent. */
	public void removeAgent(int type, GeneticCode genes) {
		for (int i = 0; i < geneCount; i++) {
			total_gene_status[type][i] -= genes.getStatus(i);
			gene_status_distribution[type][i][geneStatusHash(genes.getValue(i))]--;
			gene_value_distribution[type][i][genes.getValue(i)]--;
		}
		total_agents[type]--;
	}

	/** Calculates GA info and prints them if appropriate. */
	public void printGAInfo(long time_step) {
		plotGeneValueDistribution(time_step);
	}
	private int frameskip;

	/** Plot the gene value distribution of all agent types for a certain time step. */
	private void plotGeneValueDistribution(long time_step) {
		if (frameskip-- <= 0) {

			charOutput.updateGeneStatusDistributionData(gene_value_distribution, gene_status_distribution);
			frameskip = update_frequency;
		}
	}

	/** Gets the appropriate index of a gene of a specific value in a gene status distribution hash table (or array). */
	public int geneStatusHash(int gene_value) {
		int index;
		if (gene_value > 90) {
			index = Math.abs(180 - gene_value);
		} else {
			index = gene_value;
		}
		return index;
	}

	/** Returns the current state of 'track_gene_value_distribution'. */
	public boolean getTrackGeneValueDistribution() {
		return track_gene_value_distribution;
	}

	/** Sets the state of 'track_gene_value_distribution'. */
	public void setTrackGeneValueDistribution(boolean state) {
		track_gene_value_distribution = state;
	}

	public void tickNotification(long time) {
		/* If program is set to track GA info, then print them. */
		if (track_gene_value_distribution) {
			printGAInfo(time);
		}
	}

	public void tickZero() {
		tickNotification(0);
	}

	public double getAvgStatus(int agentType, int geneType) {
		return total_gene_status[agentType][geneType] / total_agents[agentType];
	}

}
