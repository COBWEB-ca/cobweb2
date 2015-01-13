package org.cobweb.cobweb2.abiotic;

import java.util.Arrays;
import java.util.Collection;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.StatePluginSource;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ParameterCustomSerializable;
import org.cobweb.io.ParameterSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TemperatureParams implements ParameterCustomSerializable, StatePluginSource {

	public static final int TEMPERATURE_BANDS = 5;

	private static final long serialVersionUID = -4024670457346662550L;

	/**
	 * An area where the temperature is constant.
	 */
	@ConfDisplayName("Band")
	public float[] tempBands = new float[TEMPERATURE_BANDS];

	public TemperatureAgentParams[] agentParams;

	private final AgentFoodCountable env;

	/**
	 * Constructor sets the environment parameters, and temperature agent type
	 * parameters.
	 *
	 * @param env Environment parameters.
	 */
	public TemperatureParams(AgentFoodCountable env) {
		this.env = env;

		this.agentParams = new TemperatureAgentParams[env.getAgentTypes()];

		for (int i = 0; i < agentParams.length; i++) {
			agentParams[i] = new TemperatureAgentParams();
		}
	}

	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {

		agentParams = new TemperatureAgentParams[env.getAgentTypes()];

		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals("TempBands")) {
				NodeList nl2 = n.getChildNodes();
				for (int j = 0; j < nl2.getLength(); j++) {
					Node tt = nl2.item(j);
					tempBands[j] = Float.parseFloat(tt.getTextContent());
				}
			} else if (n.getNodeName().equals("AgentParams")) {
				NodeList nl2 = n.getChildNodes();
				for (int j = 0; j < nl2.getLength(); j++) {
					Node tt = nl2.item(j);
					if (j >= env.getAgentTypes())
						break;
					agentParams[j] = new TemperatureAgentParams();
					ParameterSerializer.load(agentParams[j], tt);
				}
			}
		}
		for (int i = 0; i < agentParams.length; i++) {
			if (agentParams[i] == null)
				agentParams[i] = new TemperatureAgentParams();
		}
	}

	@Override
	public void saveConfig(Node root, Document document) {
		Node bands = document.createElement("TempBands");
		for (int i = 0; i < tempBands.length; i++) {
			Node n = document.createElement("Band" + (i + 1));
			n.setTextContent(Float.toString(tempBands[i]));
			bands.appendChild(n);
		}
		root.appendChild(bands);
		Node agents = document.createElement("AgentParams");
		for (int i = 0; i < agentParams.length; i++) {
			Node n = document.createElement("Agent" + (i + 1));
			ParameterSerializer.save(agentParams[i], n, document);
			agents.appendChild(n);
		}
		root.appendChild(agents);
	}

	public void resize(AgentFoodCountable envParams) {
		TemperatureAgentParams[] n = Arrays.copyOf(agentParams, envParams.getAgentTypes());

		for (int i = agentParams.length; i < envParams.getAgentTypes(); i++) {
			n[i] = new TemperatureAgentParams();
		}
		agentParams = n;
	}

	static final String STATE_NAME_ABIOTIC_PENALTY = "Abiotic Penalty";
	@Override
	public Collection<String> getStatePluginKeys() {
		return Arrays.asList(STATE_NAME_ABIOTIC_PENALTY);
	}
}
