package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;

public class AbioticState implements AgentState {

	@ConfXMLTag("FactorStates")
	@ConfList(indexName = "Factor", startAtOne = true)
	public AbioticFactorState[] factorStates = new AbioticFactorState[0];

	@Deprecated // for reflection use only!
	public AbioticState() {
	}

	public AbioticState(AbioticAgentParams params) {
		this.factorStates = new AbioticFactorState[params.factorParams.length];
		for (int i = 0;i < params.factorParams.length; i++) {
			factorStates[i] = new AbioticFactorState(params.factorParams[i]);
		}
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 1L;
}