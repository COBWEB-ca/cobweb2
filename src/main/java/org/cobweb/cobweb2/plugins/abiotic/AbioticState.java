package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;

public class AbioticState implements AgentState {

	@ConfXMLTag("originalParamValue")
	public float originalParamValue;

	@ConfXMLTag("AgentParams")
	public AbioticAgentParams agentParams;

	@Deprecated // for reflection use only!
	public AbioticState() {
	}

	public AbioticState(AbioticAgentParams aPar) {
		agentParams = aPar;
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 1L;
}