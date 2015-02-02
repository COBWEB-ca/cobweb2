package org.cobweb.cobweb2.plugins.genetics;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.AgentSimilarityCalculator;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.core.RandomSource;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.LoggingMutator;
import org.cobweb.cobweb2.plugins.SpawnMutator;
import org.cobweb.cobweb2.plugins.StatefulMutatorBase;

/**
 * GeneticsMutator is an instance of SpawnMutator.
 *
 * @see SpawnMutator
 */
public class GeneticsMutator extends StatefulMutatorBase<GeneticCode> implements SpawnMutator, LoggingMutator, AgentSimilarityCalculator {

	private GeneticParams params;

	private GATracker tracker;

	private RandomSource simulation;


	public GeneticsMutator() {
		super(GeneticCode.class);
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
		return NO_DATA;
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
		return NO_DATA;
	}

	private void mutateAgentAttributes(Agent agent) {
		for (int i = 0; i < params.phenotype.length; i++) {
			GeneticCode gc = getAgentState(agent);

			Phenotype pheno = params.phenotype[i];

			// Get the appropriate coefficient associated with the gene value
			// Coefficient = absolute value of 2*sin(x), x being attribute value
			// in degrees
			float coefficient = gc.getStatus(i);

			// Get instance variable linked to attribute in agent
			pheno.modifyValue(agent, coefficient, 0);
		}
		tracker.addAgent(agent.getType(), getAgentState(agent));
	}

	@Override
	public void onDeath(Agent agent) {
		GeneticCode agentState = getAgentState(agent);
		if (agentState != null)
			tracker.removeAgent(agent.getType(), agentState);

	}

	@Override
	public void onSpawn(Agent agent) {
		GeneticCode genetic_code = new GeneticCode(params.agentParams[agent.getType()]);
		setAgentState(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent) {
		GeneticCode genetic_code = new GeneticCode(getAgentState(parent));

		mutateAndSave(agent, ((ComplexAgent)parent).params.mutationRate, genetic_code);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent1, Agent parent2) {
		GeneticCode genetic_code = null;
		GeneticCode gc1 = getAgentState(parent1);
		GeneticCode gc2 = getAgentState(parent2);

		// if parent2 is dead, the GeneticCode got removed in onDeath()
		// TODO: keep GeneticCode inside Agent so it doesn't get disposed while we still have parent2 reference?
		if (gc2 == null) {
			assert !parent2.isAlive() : "parent2 has no genes but is alive";
			gc2 = gc1;
		}

		if (gc1.getNumGenes() != gc2.getNumGenes())
			throw new IllegalArgumentException("Agents must have the same number of genes");

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

		setAgentState(agent, genetic_code);
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
		return GeneticCode.compareGeneticSimilarity(getAgentState(a1), getAgentState(a2));
	}

}
