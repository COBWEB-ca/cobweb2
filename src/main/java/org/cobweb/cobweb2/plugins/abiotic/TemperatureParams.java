package org.cobweb.cobweb2.plugins.abiotic;

import java.util.Arrays;
import java.util.Collection;

import org.cobweb.cobweb2.core.StatePluginSource;
import org.cobweb.cobweb2.impl.AgentFoodCountable;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

public class TemperatureParams implements ParameterSerializable, StatePluginSource {

	public static final int TEMPERATURE_BANDS = 5;

	private static final long serialVersionUID = -4024670457346662550L;

	/**
	 * An area where the temperature is constant.
	 */
	@ConfDisplayName("Band")
	@ConfXMLTag("TempBands")
	@ConfList(indexName = "Band", startAtOne = true)
	public float[] tempBands = new float[TEMPERATURE_BANDS];

	@ConfXMLTag("AgentParams")
	@ConfList(indexName = "Agent", startAtOne = true)
	public TemperatureAgentParams[] agentParams = new TemperatureAgentParams[0];

	/**
	 * Constructor sets the environment parameters, and temperature agent type
	 * parameters.
	 *
	 * @param env Environment parameters.
	 */
	public TemperatureParams(AgentFoodCountable env) {
		resize(env);
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
