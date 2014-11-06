package disease;

import ga.GeneticParams.Phenotype;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;
import cwcore.complexParams.AgentFoodCountable;

public class DiseaseParams extends AbstractReflectionParams {

	private static final long serialVersionUID = 6866958975246266955L;

	@ConfXMLTag("Index")
	public int type;

	/**
	 * Fraction of initially infected agents.
	 */
	@ConfXMLTag("initialInfection")
	@ConfDisplayName("Initially infected fraction")
	public float initialInfection = 0;

	/**
	 * Agent types this agent can transmit the disease to.
	 */
	@ConfDisplayName("Transmit to")
	public boolean[] transmitTo;

	/**
	 * Chance this agent will get a disease from contact with an infected agent.
	 */
	@ConfXMLTag("contactTransmitRate")
	@ConfDisplayName("Contact transmission rate")
	public float contactTransmitRate = 0.5f;

	/**
	 * Chance a child of an infected agent will be infected.
	 */
	@ConfXMLTag("childTransmitRate")
	@ConfDisplayName("Child transmission rate")
	public float childTransmitRate = 0.9f;

	/**
	 * Which parameter is affected by the disease.
	 */
	@ConfXMLTag("parameter")
	@ConfDisplayName("Parameter")
	public Phenotype param = new Phenotype();

	/**
	 * The factor the parameter is multiplied by when the agent is infected.
	 */
	@ConfXMLTag("factor")
	@ConfDisplayName("Factor")
	public float factor = 2;

	private final AgentFoodCountable env;

	private static final Pattern agentTransmit = Pattern.compile("^agent(\\d+)$");

	public DiseaseParams(AgentFoodCountable env) {
		this.env = env;
		transmitTo = new boolean[env.getAgentTypes()];
	}

	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		super.loadConfig(root);

		transmitTo = new boolean[env.getAgentTypes()];

		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (!n.getNodeName().equals("transmitTo"))
				continue;
			NodeList nl2 = n.getChildNodes();
			for (int j = 0; j < nl2.getLength(); j++) {
				Node tt = nl2.item(j);
				String name = tt.getNodeName();

				Matcher m = agentTransmit.matcher(name);
				if (!m.matches())
					continue;

				int id = Integer.parseInt(m.group(1)) - 1;
				if (id >= env.getAgentTypes())
					continue;
				boolean transmit = Boolean.parseBoolean(tt.getTextContent());
				transmitTo[id] = transmit;
			}
		}
	}

	@Override
	public void saveConfig(Node root, Document document) {
		super.saveConfig(root, document);

		Node trans = document.createElement("transmitTo");
		for (int i = 0; i < transmitTo.length; i++) {
			Node n = document.createElement("agent" + (i + 1));
			n.setTextContent(Boolean.toString(transmitTo[i]));
			trans.appendChild(n);
		}
		root.appendChild(trans);

	}

	public void resize(AgentFoodCountable envParams) {
		boolean[] n = Arrays.copyOf(transmitTo, env.getAgentTypes());
		this.transmitTo = n;
	}

	@ConfXMLTag("vaccinator")
	@ConfDisplayName("Vaccinator")
	public boolean vaccinator = false;

	@ConfXMLTag("vaccineEffectiveness")
	@ConfDisplayName("Vaccine Effectiveness")
	public float vaccineEffectiveness = 1.0f;

	@ConfXMLTag("healer")
	@ConfDisplayName("Healer")
	public boolean healer = false;

	@ConfXMLTag("healerEffectiveness")
	@ConfDisplayName("Healing Effectiveness")
	public float healerEffectiveness = 1.0f;

	@ConfXMLTag("recoveryTime")
	@ConfDisplayName("Recovery time")
	public int recoveryTime = 0;

}
