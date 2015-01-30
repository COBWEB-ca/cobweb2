package org.cobweb.cobweb2.plugins.pd;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;



public class PDState implements AgentState {
	/**
	 * This agent's last play was cheating
	 */
	@ConfXMLTag("pdCheater")
	public boolean pdCheater;

	/**
	 * This agent's last opponent's action was cheating
	 */
	@ConfXMLTag("lastPDcheated")
	public boolean lastPDcheated;

	@ConfXMLTag("AgentParams")
	public PDAgentParams agentParams;

	@Deprecated // For reflection use only! Initialize transient fields after.
	public PDState() {
	}

	public PDState(PDAgentParams pdAgentParams) {
		this.agentParams = pdAgentParams;
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 1L;
}

