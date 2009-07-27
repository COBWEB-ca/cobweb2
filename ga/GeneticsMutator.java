package ga;

import ga.GeneticParams.Phenotype;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.HashMap;
import java.util.Map;

import cwcore.AgentSimularityCalculator;
import cwcore.ComplexAgent;
import cwcore.complexParams.AgentParamsMutator;

public class GeneticsMutator implements AgentParamsMutator, AgentSimularityCalculator {

	private GeneticParams params;

	public GeneticsMutator(GeneticParams params, int agentCount) {
		this.params = params;
		this.tracker = new GATracker(agentCount, params.geneCount, params.trackValues, params.updateFrequency);
	}

	private GATracker tracker;

	public GATracker getTracker() {
		return tracker;
	}
	
	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2) {
		GeneticCode genetic_code = null;
		switch(params.meiosisMode.mode) {
			case ColourAveraging:
				genetic_code = GeneticCode.createGeneticCodeMeiosisAverage(genes.get(parent1), genes.get(parent2));
				break;
			case GeneSwapping:
				genetic_code = GeneticCode.createGeneticCodeMeiosisGeneSwap(genes.get(parent1), genes.get(parent2));
				break;
			case RandomRecombination:
				genetic_code = GeneticCode.createGeneticCodeMeiosisRecomb(genes.get(parent1), genes.get(parent2));
				break;
		}

		if (cobweb.globals.random.nextFloat() < parent1.params.mutationRate) {
			genetic_code.mutate(cobweb.globals.random.nextInt(params.geneCount * params.geneLength));
		}

		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		GeneticCode genetic_code = new GeneticCode(genes.get(parent));

		if (cobweb.globals.random.nextFloat() <= parent.params.mutationRate) {
			genetic_code.mutate(cobweb.globals.random.nextInt(params.geneCount * params.geneLength));
		}

		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	public void onSpawn(ComplexAgent agent) {
		GeneticCode genetic_code = new GeneticCode(params.geneCount);
		for (int i = 0; i < params.geneCount; i++) {
			genetic_code.bitsFromString(i * 8, 8, params.geneValues[agent.type()][i], 0);
		}
		genes.put(agent, genetic_code);
		mutateAgentAttributes(agent);
	}

	private Map<ComplexAgent, GeneticCode> genes = new HashMap<ComplexAgent, GeneticCode>();

	public void mutateAgentAttributes(ComplexAgent agent) {
		float[] colValues = new float[3];
		for (int i = 0; i < params.phenotype.length; i++) {
			GeneticCode gc = genes.get(agent);
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
			try {
				Object o;
				o = pheno.field.get(agent.params);

				// Modify the value according to the coefficient.
				if (o instanceof Float) {
					float value = ((Float)o).floatValue();
					pheno.field.setFloat(agent.params, value * coefficient);
				} else if (o instanceof Integer) {
					double value = ((Integer) o).doubleValue();
					pheno.field.setInt(agent.params, (int)Math.round(value * coefficient));
				} else {
					throw new IllegalArgumentException("Unknown phenotype field type");
				}
			} catch (IllegalAccessException ex) {
				throw new RuntimeException("Cannot access field: " + pheno.field.toString(), ex);
			}

		}
		Color col = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), colValues, 1);
		agent.setColor(col);
		tracker.addAgent(agent.type(), genes.get(agent));
	}

	public void onDeath(ComplexAgent agent) {
		if (genes.containsKey(agent))
			tracker.removeAgent(agent.type(), genes.get(agent));
	}

	public float similarity(ComplexAgent a1, ComplexAgent a2) {
		return GeneticCode.compareGeneticSimilarity(genes.get(a1), genes.get(a2));
	}

}
