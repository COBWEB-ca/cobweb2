package org.cobweb.cobweb2.plugins.waste;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;

public class WasteState implements AgentState {

	@ConfXMLTag("energyLost")
	public int energyLost;

	@ConfXMLTag("energyGained")
	public int energyGained;

	@ConfXMLTag("AgentParams")
	public WasteAgentParams agentParams;

	@Deprecated // for reflection use only!
	public WasteState() {
	}

	public WasteState(WasteAgentParams wasteAgentParams) {
		this.agentParams = wasteAgentParams;
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 1L;
}