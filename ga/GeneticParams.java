/**
 *
 */
package ga;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.CobwebParam;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;
import cwcore.complexParams.ComplexAgentParams;
import cwcore.complexParams.ComplexEnvironmentParams;
import cwcore.complexParams.GeneMutatable;


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


	public static class Phenotype implements CobwebParam {

		private static final long serialVersionUID = -6142169580857190598L;

		public Field field = null;

		private static Iterable<Field> bindables = new LinkedList<Field>() {
			private static final long serialVersionUID = -6369342528741543712L;
			{
				for (Field f: ComplexAgentParams.class.getFields()) {
					if (f.getAnnotation(GeneMutatable.class) == null)
						continue;
					this.add(f);
				}
			}
		};

		public static Iterable<Field> getBindables() {
			return bindables;
		}

		public Phenotype(){
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Phenotype) {
				Phenotype p = (Phenotype) obj;
				if (p.field == null && this.field == null) return true;
				if (p.field == null || this.field == null) return false;
				return p.field.equals(this.field);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return field.hashCode();
		}

		public Phenotype(Field f) {
			if (f.getAnnotation(GeneMutatable.class) == null || f.getAnnotation(ConfDisplayName.class) == null)
				throw new IllegalArgumentException("Field must be labeled as @GeneMutatable and have a @ConfDisplayName");
			this.field = f;
		}

		public void loadConfig(Node root) throws IllegalArgumentException {
			String value = root.getTextContent();
			for (Field f : bindables) {
				if (value.equals("None")) {
					this.field = null;
					return;
				}
				if (f.getAnnotation(ConfXMLTag.class).value().equals(value)) {
					this.field = f;
					return;
				}
				if (f.getAnnotation(ConfDisplayName.class).value().equals(value)) {
					this.field = f;
					return;
				}
			}
			throw new IllegalArgumentException("Cannot match Phenotype '" + value + "' to any field");
		}

		@Override
		public String toString() {
			if (field == null)
				return "[No phenotype]";
			else
				return field.getAnnotation(ConfDisplayName.class).value();
		}

		public void saveConfig(Node root, Document document) {
			String value;
			if (field == null) {
				value = "None";
			} else {
				value = field.getAnnotation(ConfXMLTag.class).value();
			}
			root.setTextContent(value);
		}

	}

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
			private String value;
			private MeiosisMode(String s) {
				value = s;
			}

			public static MeiosisMode fromString(String s) {
				for (MeiosisMode m : EnumSet.allOf(MeiosisMode.class)) {
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

		public void loadConfig(Node root) throws IllegalArgumentException {
			mode = MeiosisMode.fromString(root.getTextContent());
		}

		public void saveConfig(Node root, Document document) {
			root.setTextContent(mode.toString());
		}
	}

	@ConfDisplayName("Meiosis Mode")
	@ConfXMLTag("meiosismode")
	public MeiosisModeParam meiosisMode ;

	@ConfDisplayName("Watch gene values")
	@ConfXMLTag("trackgenevaluedistribution")
	public boolean trackValues;

	@ConfDisplayName("Chart update frequency")
	@ConfXMLTag("chartupdatefrequency")
	public int updateFrequency;

	private ComplexEnvironmentParams env;


	/**
	 * geneValues[agent][gene]
	 */
	public String[][] geneValues;

	private static final Pattern geneValRE = Pattern.compile("agent(\\d+)gene(\\d+)");
	private static final Pattern phenotypeRE = Pattern.compile("linkedphenotype(\\d+)");

	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		super.loadConfig(root);

		phenotype = new Phenotype[geneCount];

		geneValues = new String[env.agentTypeCount][geneCount];

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
					geneValues[agent][gene] = n.getFirstChild().getNodeValue();
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

		for (int agent = 0; agent < env.agentTypeCount; agent++) {
			for (int gene = 0; gene < geneCount; gene++) {
				Node n = document.createElement(String.format("agent%dgene%d", agent + 1, gene + 1));
				n.setTextContent(geneValues[agent][gene]);
				root.appendChild(n);
			}
		}
	}


	public GeneticParams(ComplexEnvironmentParams env) {
		this.env = env;
		geneCount = 3;
		geneLength = 8;

		phenotype = new Phenotype[geneCount];
		for (int i = 0; i < geneCount; i++)
			phenotype[i] = new Phenotype();
		meiosisMode = new MeiosisModeParam();
		trackValues = false;

		geneValues = new String[env.agentTypeCount][geneCount];
		for (int i = 0; i < env.agentTypeCount; i++)
			for (int j = 0; j < geneCount; j++)
				geneValues[i][j] = "00011110";
		updateFrequency = 10;
	}




}