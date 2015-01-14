/**
 *
 */
package org.cobweb.cobweb2.genetics;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cobweb.cobweb2.core.params.AgentFoodCountable;
import org.cobweb.cobweb2.interconnect.Phenotype;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfSquishParent;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterCustomSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GeneticParams implements ParameterCustomSerializable {
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


	private final transient AgentFoodCountable env;


	/**
	 * geneValues[agent][gene]
	 */
	public String[][] geneValues = new String[0][0];

	private static final Pattern geneValRE = Pattern.compile("agent(\\d+)gene(\\d+)");

	/**
	 *
	 */
	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		geneValues = new String[env.getAgentTypes()][getGeneCount()];

		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);

			Matcher m = geneValRE.matcher(n.getNodeName());
			if (m.matches()) {
				int agent = Integer.parseInt(m.group(1)) - 1;
				int gene = Integer.parseInt(m.group(2)) - 1;
				if (agent >= env.getAgentTypes() || gene >= getGeneCount())
					continue;
				geneValues[agent][gene] = n.getFirstChild().getNodeValue();
			}
		}
		for (int i = 0; i < geneValues.length; i++) {
			for (int j = 0; j < geneValues[i].length; j++) {
				if (geneValues[i][j] == null)
					geneValues[i][j] = Integer.toBinaryString(30);
			}
		}
	}

	@Override
	public void saveConfig(Node root, Document document) {
		for (int agent = 0; agent < geneValues.length; agent++) {
			for (int gene = 0; gene < getGeneCount(); gene++) {
				Node n = document.createElement(String.format("agent%dgene%d", new Integer(agent + 1), new Integer(gene + 1)));
				n.setTextContent(geneValues[agent][gene]);
				root.appendChild(n);
			}
		}
	}


	public GeneticParams(AgentFoodCountable env) {
		this.env = env;
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
