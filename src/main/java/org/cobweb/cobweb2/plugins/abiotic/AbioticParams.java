package org.cobweb.cobweb2.plugins.abiotic;

import java.util.Arrays;
import java.util.Collection;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.StatePluginSource;
import org.cobweb.cobweb2.plugins.PerAgentParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;

public class AbioticParams extends PerAgentParams<AbioticAgentParams> implements StatePluginSource {

	public static final int TEMPERATURE_BANDS = 5;

	/**
	 * An area where the temperature is constant.
	 */
	@ConfDisplayName("Band")
	@ConfXMLTag("TempBands")
	@ConfList(indexName = "Band", startAtOne = true)
	public float[] tempBands = new float[0];

	/**
	 * Constructor sets the environment parameters, and temperature agent type
	 * parameters.
	 *
	 * @param size Environment parameters.
	 */
	public AbioticParams(AgentFoodCountable size) {
		super(AbioticAgentParams.class);
		resize(size);
	}

	@Override
	protected AbioticAgentParams newAgentParam() {
		return new AbioticAgentParams();
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		super.resize(envParams);
		tempBands = Arrays.copyOf(tempBands, TEMPERATURE_BANDS);
	}


	static final String STATE_NAME_ABIOTIC_PENALTY = "Abiotic Penalty";
	@Override
	public Collection<String> getStatePluginKeys() {
		return Arrays.asList(STATE_NAME_ABIOTIC_PENALTY);
	}

	private static final long serialVersionUID = 2L;
}
