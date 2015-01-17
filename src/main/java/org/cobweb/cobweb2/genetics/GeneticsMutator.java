package org.cobweb.cobweb2.genetics;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cobweb.cobweb2.RandomSource;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.AgentSimilarityCalculator;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.interconnect.Phenotype;
import org.cobweb.cobweb2.interconnect.SpawnMutator;
import org.cobweb.io.ConfDisplayName;

/**
 * GeneticsMutator is an instance of SpawnMutator.
 *
 * @see SpawnMutator
 */
public class GeneticsMutator implements SpawnMutator, AgentSimilarityCalculator {

	private GeneticParams params;

	private GATracker tracker;

	private Map<Agent, GeneticCode> genes = new HashMap<Agent, GeneticCode>();

	private RandomSource simulation;

	private static final Collection<String> blank = new LinkedList<String>();

	/**
	 * Returns the genes of agent.  If the agent does not currently have genes, they will be created.
	 *
	 * @param agent The complex agent that the genes will come from.
	 * @return The genetic code of agent
	 */
	public GeneticCode getGene(Agent agent) {
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
		for (int i = 0; i < params.getGeneCount(); i++) {
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
		for (int i = 0; i < params.getGeneCount(); i++) {
			s.add("Avg. Gene: " + params.phenotype[i]);
		}
		return s;
	}

	@Override
	public Collection<String> logHeaderTotal() {
		return blank;
	}

	private void mutateAgentAttributes(Agent agent) {
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
			pheno.modifyValue(agent, coefficient, 0);
		}
		tracker.addAgent(agent.getType(), getGene(agent));
	}

	@Override
	public void onDeath(Agent agent) {
		if (genes.containsKey(agent)) {
			tracker.removeAgent(agent.getType(), genes.get(agent));
			genes.remove(agent);
		}
	}

	@Override
	public void onSpawn(Agent agent) {
		GeneticCode genetic_code = new GeneticCode(params.getGeneCount());
		for (int i = 0; i < params.getGeneCount(); i++) {
			genetic_code.bitsFromString(i * 8, 8, params.geneValues[agent.getType()][i], 0);
		}
		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent) {
		GeneticCode genetic_code = new GeneticCode(getGene(parent));

		mutateAndSave(agent, ((ComplexAgent)parent).params.mutationRate, genetic_code);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent1, Agent parent2) {
		GeneticCode genetic_code = null;
		GeneticCode gc1 = getGene(parent1);
		GeneticCode gc2 = getGene(parent2);

		// if parent2 is dead, the GeneticCode got removed in onDeath()
		// TODO: keep GeneticCode inside Agent so it doesn't get disposed while we still have parent2 reference?
		if (gc2 == null) {
			assert !parent2.isAlive() : "parent2 has no genes but is alive";
			gc2 = gc1;
		}

		switch (params.meiosisMode) {
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

		mutateAndSave(agent, ((ComplexAgent)parent1).params.mutationRate, genetic_code);
	}

	protected void mutateAndSave(Agent agent, float mutationRate, GeneticCode genetic_code) {
		if (genetic_code.getNumGenes() > 0) {
			if (simulation.getRandom().nextFloat() <= mutationRate) {
				genetic_code.mutate(simulation.getRandom().nextInt(params.getGeneCount() * params.geneLength));
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

		tracker.setParams(agentCount, params.getGeneCount());
	}

	@Override
	public float similarity(Agent a1, Agent a2) {
		return GeneticCode.compareGeneticSimilarity(getGene(a1), getGene(a2));
	}

}
