package org.cobweb.cobweb2.impl.learning;

import java.util.Arrays;

import org.cobweb.cobweb2.impl.AgentFoodCountable;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;


public class LearningParams implements ParameterSerializable {
	private static final long serialVersionUID = 2682098543563943839L;

	@ConfXMLTag("AgentParams")
	@ConfList(indexName = "Agent", startAtOne = true)
	public LearningAgentParams[] learningParams = new LearningAgentParams[0];

	public LearningParams(AgentFoodCountable env) {
		resize(env);
	}

	public void resize(AgentFoodCountable envParams) {
		LearningAgentParams[] n = Arrays.copyOf(learningParams, envParams.getAgentTypes());

		for (int i = learningParams.length; i < envParams.getAgentTypes(); i++) {
			n[i] = new LearningAgentParams();
		}
		learningParams = n;
	}
}
