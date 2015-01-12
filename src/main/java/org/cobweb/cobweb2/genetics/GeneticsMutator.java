package org.cobweb.cobweb2.genetics;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cobweb.cobweb2.RandomSource;
import org.cobweb.cobweb2.core.AgentSimilarityCalculator;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.interconnect.Phenotype;
import org.cobweb.cobweb2.interconnect.SpawnMutator;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.util.ReflectionUtil;

/**
 * GeneticsMutator is an instance of SpawnMutator.
 *
 * @see SpawnMutator
 */
public class GeneticsMutator implements SpawnMutator, AgentSimilarityCalculator {

	private GeneticParams params;

	private GATracker tracker;

	private Map<ComplexAgent, GeneticCode> genes = new HashMap<ComplexAgent, GeneticCode>();

	private RandomSource simulation;

	private static final Collection<String> blank = new LinkedList<String>();

	/**
	 * Returns the genes of agent.  If the agent does not currently have genes, they will be created.
	 *
	 * @param agent The complex agent that the genes will come from.
	 * @return The genetic code of agent
	 */
	public GeneticCode getGene(ComplexAgent agent) {
		if (!agent.isAlive())
			return null;

		GeneticCode gc = genes.get(agent);
		if (gc == null)
			onSpawn(agent);

		return gc;
	}

	public GATracker getTracker() {
		return tracker;
	}

	@Override
	public Collection<String> logDataAgent(int agentType) {
		List<String> s = new LinkedList<String>();
		for (int i = 0; i < params.geneCount; i++) {
			s.add(Double.toString(tracker.getAvgStatus(agentType, i)));
		}
		return s;
	}

	@Override
	public Collection<String> logDataTotal() {
		return blank;
	}

	@Override
	public Collection<String> logHeadersAgent() {
		List<String> s = new LinkedList<String>();
		for (int i = 0; i < params.geneCount; i++) {
			s.add("Avg. Gene: " + params.phenotype[i].field.getAnnotation(ConfDisplayName.class).value());
		}
		return s;
	}

	@Override
	public Collection<String> logHeaderTotal() {
		return blank;
	}

	private void mutateAgentAttributes(ComplexAgent agent) {
		for (int i = 0; i < params.phenotype.length; i++) {
			GeneticCode gc = getGene(agent);

			Phenotype pheno = params.phenotype[i];
			if (pheno.field == null)
				continue;

			// Get the appropriate coefficient associated with the gene value
			// Coefficient = absolute value of 2*sin(x), x being attribute value
			// in degrees
			float coefficient = gc.getStatus(i);

			// Get instance variable linked to attribute in agent
			// TODO LOW different formulas for different types of fields?
			ReflectionUtil.multiplyField(agent.params, pheno.field, coefficient);
		}
		tracker.addAgent(agent.getType(), getGene(agent));
	}

	@Override
	public void onDeath(ComplexAgent agent) {
		if (genes.containsKey(agent)) {
			tracker.removeAgent(agent.getType(), genes.get(agent));
			genes.remove(agent);
		}
	}

	@Override
	public void onSpawn(ComplexAgent agent) {
		GeneticCode genetic_code = new GeneticCode(params.geneCount);
		for (int i = 0; i < params.geneCount; i++) {
			genetic_code.bitsFromString(i * 8, 8, params.geneValues[agent.getType()][i], 0);
		}
		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	@Override
	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		GeneticCode genetic_code = new GeneticCode(getGene(parent));

		if (params.geneCount > 0) {
			if (simulation.getRandom().nextFloat() <= parent.params.mutationRate) {
				genetic_code.mutate(simulation.getRandom().nextInt(params.geneCount * params.geneLength));
			}
		}

		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	@Override
	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2) {
		GeneticCode genetic_code = null;
		GeneticCode gc1 = getGene(parent1);
		GeneticCode gc2 = getGene(parent2);
		//TODO deal with nulls. already done?

		if (gc1 == null && gc2 == null) {
			gc1 = new GeneticCode(params.geneCount);
			gc2 = new GeneticCode(params.geneCount);
		} else if (gc1 == null) {
			gc1 = gc2;
		} else if (gc2 == null) {
			gc2 = gc1;
		}

		switch (params.meiosisMode.mode) {
			case ColourAveraging:
				genetic_code = GeneticCode.createGeneticCodeMeiosisAverage(gc1, gc2);
				break;
			case GeneSwapping:
				genetic_code = GeneticCode.createGeneticCodeMeiosisGeneSwap(gc1, gc2, simulation.getRandom());
				break;
			case RandomRecombination:
			default:
				genetic_code = GeneticCode.createGeneticCodeMeiosisRecomb(gc1, gc2, simulation.getRandom());
				break;
		}

		if (genetic_code.getNumGenes() > 0) {
			if (simulation.getRandom().nextFloat() < parent1.params.mutationRate) {
				genetic_code.mutate(simulation.getRandom().nextInt(params.geneCount * params.geneLength));
			}
		}

		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	/**
	 *
	 *
	 * @param params The parameters used in the simulation data file (xml file).
	 * @param agentCount The number of agent types.
	 */
	public void setParams(RandomSource rand, GeneticParams params, int agentCount) {
		simulation = rand;
		this.params = params;
		if (tracker == null)
			this.tracker = new GATracker();

		tracker.setParams(agentCount, params.geneCount);
	}

	@Override
	public float similarity(ComplexAgent a1, ComplexAgent a2) {
		return GeneticCode.compareGeneticSimilarity(getGene(a1), getGene(a2));
	}

}
