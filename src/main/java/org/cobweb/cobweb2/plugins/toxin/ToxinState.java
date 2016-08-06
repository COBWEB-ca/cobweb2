package org.cobweb.cobweb2.plugins.toxin;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;


public class ToxinState implements AgentState {

	@ConfXMLTag("toxicity")
	public float toxicity = 0;

	@ConfXMLTag("poisoned")
	public boolean poisoned = false;

	@ConfXMLTag("AgentParams")
	public ToxinAgentParams agentParams;

	@Deprecated // for reflection use only!
	public ToxinState() {
	}

	public ToxinState(ToxinAgentParams agentParams, float initialToxicity) {
		this.agentParams = agentParams;
		this.toxicity = initialToxicity;
		if (toxicity > agentParams.toxicityThreshold.getValue())
			poisoned = true;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	private static final long serialVersionUID = 1L;
}
