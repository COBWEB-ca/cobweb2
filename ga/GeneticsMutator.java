package ga;

import ga.GeneticParams.Phenotype;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cobweb.params.ConfDisplayName;
import cobweb.params.ReflectionUtil;
import cwcore.AgentSimularityCalculator;
import cwcore.ComplexAgent;
import cwcore.complexParams.SpawnMutator;

public class GeneticsMutator implements SpawnMutator, AgentSimularityCalculator {

	private GeneticParams params;

	private GATracker tracker;

	private Map<ComplexAgent, GeneticCode> genes = new HashMap<ComplexAgent, GeneticCode>();

	public GeneticsMutator() {
	}

	public GATracker getTracker() {
		return tracker;
	}

	public void mutateAgentAttributes(ComplexAgent agent) {
		float[] colValues = new float[3];
		for (int i = 0; i < params.phenotype.length; i++) {
			GeneticCode gc = getGene(agent);
			int gene_value = gc.getValue(i);
			if (i < 3) {
				colValues[i] = gene_value / 255f;
			}

			Phenotype pheno = params.phenotype[i];
			if (pheno.field == null)
				continue;

			// Get the appropriate coefficient associated with the gene value
			// Coefficient = absolute value of 2*sin(x), x being attribute value
			// in degrees
			float coefficient = gc.getStatus(i);

			// Get instance variable linked to attribute in agent
			ReflectionUtil.multiplyField(agent.params, pheno.field, coefficient);


		}
		Color col = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), colValues, 1);
		agent.setColor(col);
		tracker.addAgent(agent.type(), getGene(agent));
	}


	private GeneticCode getGene(ComplexAgent agent) {
		GeneticCode gc = genes.get(agent);
		if (gc == null) {
			onSpawn(agent);
		}
		return gc;
	}

	public void onDeath(ComplexAgent agent) {
		if (genes.containsKey(agent))
			tracker.removeAgent(agent.type(), genes.get(agent));
	}

	public void onSpawn(ComplexAgent agent) {
		GeneticCode genetic_code = new GeneticCode(params.geneCount);
		for (int i = 0; i < params.geneCount; i++) {
			genetic_code.bitsFromString(i * 8, 8, params.geneValues[agent.type()][i], 0);
		}
		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		GeneticCode genetic_code = new GeneticCode(getGene(parent));

		if (params.geneCount > 0) {
			if (cobweb.globals.random.nextFloat() <= parent.params.mutationRate) {
				genetic_code.mutate(cobweb.globals.random.nextInt(params.geneCount * params.geneLength));
			}
		}

		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2) {
		GeneticCode genetic_code = null;
		switch (params.meiosisMode.mode) {
			case ColourAveraging:
				genetic_code = GeneticCode.createGeneticCodeMeiosisAverage(getGene(parent1), getGene(parent2));
				break;
			case GeneSwapping:
				genetic_code = GeneticCode.createGeneticCodeMeiosisGeneSwap(getGene(parent1), getGene(parent2));
				break;
			case RandomRecombination:
				genetic_code = GeneticCode.createGeneticCodeMeiosisRecomb(getGene(parent1), getGene(parent2));
				break;
		}

		if (genetic_code.getNumGenes() > 0) {
			if (cobweb.globals.random.nextFloat() < parent1.params.mutationRate) {
				genetic_code.mutate(cobweb.globals.random.nextInt(params.geneCount * params.geneLength));
			}
		}

		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	public void setParams(GeneticParams params, int agentCount) {
		this.params = params;
		if (tracker == null)
			this.tracker = new GATracker();

		String[] names = new String[params.geneCount];
		for (int i = 0; i < params.geneCount; i++) {
			names[i] = params.phenotype[i].field.getAnnotation(ConfDisplayName.class).value();
		}
		tracker.setParams(agentCount, params.geneCount, params.trackValues, params.updateFrequency, names);
	}

	public float similarity(ComplexAgent a1, ComplexAgent a2) {
		return GeneticCode.compareGeneticSimilarity(getGene(a1), getGene(a2));
	}

	@Override
	public Collection<String> logDataAgent(int agentType) {
		List<String> s = new LinkedList<String>();
		for (int i = 0; i < params.geneCount; i++) {
			s.add(Double.toString(tracker.getAvgStatus(agentType, i)));
		}
		return s;
	}

	private static final Collection<String> blank = new LinkedList<String>();

	@Override
	public Collection<String> logDataTotal() {
		return blank;
	}

	@Override
	public Collection<String> logHeaderTotal() {
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

}
