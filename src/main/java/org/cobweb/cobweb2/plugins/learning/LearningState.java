package org.cobweb.cobweb2.plugins.learning;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;


public class LearningState implements AgentState {

	@ConfXMLTag("AgentParams")
	LearningAgentParams agentParams;


	public LearningState(LearningAgentParams agentParams) {
		this.agentParams = agentParams;

	}

	@Override
	public boolean isTransient() {
		return false;
	}

}
