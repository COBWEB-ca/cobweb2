/**
 *
 */
package org.cobweb.cobweb2.plugins.genetics;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.plugins.PerAgentParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfSquishParent;
import org.cobweb.io.ConfXMLTag;


public class GeneticParams extends PerAgentParams<GeneticCode> {

	// TODO: implement different lengths?
	@ConfDisplayName("Gene length")
	@ConfXMLTag("geneLength")
	public int geneLength = 8;

	@ConfDisplayName("Meiosis Mode")
	@ConfXMLTag("meiosismode")
	public MeiosisMode meiosisMode = MeiosisMode.ColourAveraging;

	@ConfDisplayName("Phenotype ")
	@ConfSquishParent
	@ConfList(indexName = "linkedphenotype", startAtOne = true)
	public Phenotype[] phenotype = new Phenotype[0];


	public int getGeneCount() {
		return phenotype.length;
	}

	public GeneticParams(AgentFoodCountable env) {
		super(GeneticCode.class);
		resize(env);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		super.resize(envParams);
		resizeGenes();
	}

	public void resizeGenes() {
		for(GeneticCode a : agentParams) {
			a.setGeneCount(getGeneCount());
		}
	}

	@Override
	protected GeneticCode newAgentParam() {
		return new GeneticCode(phenotype.length);
	}

	private static final long serialVersionUID = 2L;
}
