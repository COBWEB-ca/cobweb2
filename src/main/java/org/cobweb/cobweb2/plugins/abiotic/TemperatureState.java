package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;

public class TemperatureState implements AgentState {

	@ConfXMLTag("originalParamValue")
	public float originalParamValue;

	@ConfXMLTag("AgentParams")
	public TemperatureAgentParams agentParams;

	@Deprecated // for reflection use only!
	public TemperatureState() {
	}

	public TemperatureState(TemperatureAgentParams aPar) {
		agentParams = aPar;
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 1L;
}