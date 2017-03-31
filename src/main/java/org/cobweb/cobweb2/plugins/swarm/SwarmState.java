package org.cobweb.cobweb2.plugins.swarm;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;


public class SwarmState implements AgentState {

	@ConfXMLTag("AgentParams")
	public SwarmAgentParams agentParams;

	@Deprecated // for reflection use only!
	public SwarmState() {
	}

	public SwarmState(SwarmAgentParams aPar) {
		this.agentParams = aPar;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	protected SwarmState clone() {
		try {
			SwarmState copy = (SwarmState) super.clone();
			copy.agentParams = this.agentParams.clone();
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
