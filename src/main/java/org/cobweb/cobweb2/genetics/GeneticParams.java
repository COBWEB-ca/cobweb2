/**
 *
 */
package org.cobweb.cobweb2.genetics;

import java.util.Arrays;

import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.core.params.AgentFoodCountable;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfSquishParent;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;


public class GeneticParams implements ParameterSerializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 4935757387466603476L;

	public int getGeneCount() {
		return phenotype.length;
	}

	@ConfDisplayName("Gene length")
	@ConfXMLTag("geneLength")
	public int geneLength;

	@ConfDisplayName("Meiosis Mode")
	@ConfXMLTag("meiosismode")
	public MeiosisMode meiosisMode = MeiosisMode.ColourAveraging;

	@ConfDisplayName("Phenotype ")
	@ConfSquishParent
	@ConfList(indexName = "linkedphenotype", startAtOne = true)
	public Phenotype[] phenotype = new Phenotype[0];



	/**
	 * geneValues[agent][gene]
	 */
	@ConfSquishParent
	@ConfList(indexName = {"agent", "gene"}, startAtOne = true)
	public String[][] geneValues = new String[0][0];

	public GeneticParams(AgentFoodCountable env) {
		geneLength = 8;

		resize(env);
	}

	public void resize(AgentFoodCountable envParams) {

		String[][] n = Arrays.copyOf(geneValues, envParams.getAgentTypes());

		for (int i = geneValues.length; i < envParams.getAgentTypes(); i++) {
			n[i] = new String[getGeneCount()];
			for (int j = 0; j < getGeneCount(); j++)
				n[i][j] = "00011110";
		}
		geneValues = n;

	}




}
