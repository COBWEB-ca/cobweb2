/**
 *
 */
package org.cobweb.cobweb2.genetics;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.interconnect.Phenotype;
import org.cobweb.cobweb2.io.AbstractReflectionParams;
import org.cobweb.cobweb2.io.CobwebParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GeneticParams extends AbstractReflectionParams {
	/**
	 *
	 */
	private static final long serialVersionUID = 4935757387466603476L;

	@ConfDisplayName("Gene count")
	@ConfXMLTag("geneCount")
	public int geneCount;

	@ConfDisplayName("Gene length")
	@ConfXMLTag("geneLength")
	public int geneLength;


	@ConfDisplayName("Phenotype ")
	public Phenotype[] phenotype;

	public static class MeiosisModeParam implements CobwebParam {
		/**
		 *
		 */
		private static final long serialVersionUID = 3831838182413236946L;

		public static enum MeiosisMode {
			ColourAveraging("Colour Averaging"),
			RandomRecombination("Random Recombination"),
			GeneSwapping("Gene Swapping");

			private final String value;

			private MeiosisMode(String s) {
				value = s;
			}

			public static MeiosisMode fromString(String s) {
				for (MeiosisMode m : MeiosisMode.values()) {
					if (m.value.equals(s))
						return m;
				}
				throw new IllegalArgumentException("Invalid value");
			}

			@Override
			public String toString() {
				return value;
			}
		}

		public MeiosisMode mode = MeiosisMode.ColourAveraging;

		@Override
		public void loadConfig(Node root) throws IllegalArgumentException {
			mode = MeiosisMode.fromString(root.getTextContent());
		}

		@Override
		public void saveConfig(Node root, Document document) {
			root.setTextContent(mode.toString());
		}
	}

	@ConfDisplayName("Meiosis Mode")
	@ConfXMLTag("meiosismode")
	public MeiosisModeParam meiosisMode;

	private AgentFoodCountable env;


	/**
	 * geneValues[agent][gene]
	 */
	public String[][] geneValues;

	private static final Pattern geneValRE = Pattern.compile("agent(\\d+)gene(\\d+)");
	private static final Pattern phenotypeRE = Pattern.compile("linkedphenotype(\\d+)");

	/**
	 * 
	 */
	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		super.loadConfig(root);

		phenotype = new Phenotype[geneCount];
		for (int i = 0; i < geneCount; i++)
			phenotype[i] = new Phenotype();

		geneValues = new String[env.getAgentTypes()][geneCount];

		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);

			Matcher m = phenotypeRE.matcher(n.getNodeName());
			if (m.matches()) {
				int index = Integer.parseInt(m.group(1)) - 1;
				phenotype[index] = new Phenotype();
				phenotype[index].loadConfig(n);
			} else {
				m = geneValRE.matcher(n.getNodeName());
				if (m.matches()) {
					int agent = Integer.parseInt(m.group(1)) - 1;
					int gene = Integer.parseInt(m.group(2)) - 1;
					if (agent >= env.getAgentTypes() || gene >= geneCount)
						continue;
					geneValues[agent][gene] = n.getFirstChild().getNodeValue();
				}
			}
		}
		for (int i = 0; i < geneValues.length; i++) {
			for (int j = 0; j < geneValues[i].length; j++) {
				if (geneValues[i][j] == null)
					geneValues[i][j] = Integer.toBinaryString(30);
			}
		}

		for (int i = 0; i < geneCount; i++) {
			if (phenotype[i].field == null) {
				for (int j = i; j < geneCount - 1; j++) {
					phenotype[i] = phenotype[i + 1];
					for (int ag = 0; ag < env.getAgentTypes(); ag++) {
						geneValues[ag][i] = geneValues[ag][i + 1];
					}
					geneCount--;
				}
			}
		}
	}

	@Override
	public void saveConfig(Node root, Document document) {
		super.saveConfig(root, document);

		for (int i = 0; i < phenotype.length; i++) {
			Node n = document.createElement("linkedphenotype" + (i + 1));
			phenotype[i].saveConfig(n, document);
			root.appendChild(n);
		}

		for (int agent = 0; agent < env.getAgentTypes(); agent++) {
			for (int gene = 0; gene < geneCount; gene++) {
				Node n = document.createElement(String.format("agent%dgene%d", new Integer(agent + 1), new Integer(gene + 1)));
				n.setTextContent(geneValues[agent][gene]);
				root.appendChild(n);
			}
		}
	}


	public GeneticParams(AgentFoodCountable env) {
		this.env = env;
		geneCount = 0;
		geneLength = 8;

		phenotype = new Phenotype[geneCount];
		for (int i = 0; i < geneCount; i++)
			phenotype[i] = new Phenotype();
		meiosisMode = new MeiosisModeParam();

		geneValues = new String[env.getAgentTypes()][geneCount];
		for (int i = 0; i < env.getAgentTypes(); i++)
			for (int j = 0; j < geneCount; j++)
				geneValues[i][j] = "00011110";
	}

	public void resize(AgentFoodCountable envParams) {

		String[][] n = Arrays.copyOf(geneValues, envParams.getAgentTypes());

		for (int i = geneValues.length; i < envParams.getAgentTypes(); i++) {
			n[i] = new String[geneCount];
			for (int j = 0; j < geneCount; j++)
				n[i][j] = "00011110";
		}
		geneValues = n;

	}




}
