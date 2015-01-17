package org.cobweb.cobweb2.genetics;

import org.cobweb.util.ArrayUtilities;


public class GATracker {



	/** The size of distribution of gene status. 91 given our |2sin(x)| function */
	public static final int GENE_STATUS_DISTRIBUTION_SIZE = 91;

	/** The size of distribution of gene valu. 256 given our 8-bit per gene system */
	public static final int GENE_VALUE_DISTRIBUTION_SIZE = 256;

	/** Number of agents stored in GATracker's 'agents' */
	private int[] total_agents;

	/** The sum of specific gene status numbers over particular agent types. */
	private double[][] total_gene_status;

	/**
	 * The distribution of the status of a specific gene over particular agent types.
	 * array indices are: [agent_type][gene_index][gene_status],
	 * value is the number of agents with given specific status
	 * */
	private double[][][] gene_status_distribution;

	/**
	 * The distribution of the value of a specific gene over particular agent types.
	 * array indices are: [agent_type][gene_index][gene_value],
	 * value is the number of agents with given specific value
	 * */
	private double[][][] gene_value_distribution;

	private int typeCount;

	private int geneCount;

	public void setParams(int agentTypes, int geneNo) {
		typeCount = agentTypes;
		geneCount = geneNo;

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
	}

	public int getAgentTypeCount() {
		return typeCount;
	}

	public int getGeneCount() {
		return geneCount;
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

	public double[][][] getGeneStatusDistribution() {
		return gene_status_distribution;
	}

	public double[][][] getGeneValueDistribution() {
		return gene_value_distribution;
	}

	/** Gets the appropriate index of a gene of a specific value in a gene status distribution hash table (or array). */
	private static int geneStatusHash(int gene_value) {
		int index;
		if (gene_value > 90) {
			index = Math.abs(180 - gene_value);
		} else {
			index = gene_value;
		}
		return index;
	}

	public double getAvgStatus(int agentType, int geneType) {
		return total_gene_status[agentType][geneType] / total_agents[agentType];
	}

}
