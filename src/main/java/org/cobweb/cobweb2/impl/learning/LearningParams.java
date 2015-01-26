package org.cobweb.cobweb2.impl.learning;

import org.cobweb.cobweb2.impl.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;


public class LearningParams extends PerAgentParams<LearningAgentParams> {

	public LearningParams(AgentFoodCountable envParams) {
		super(LearningAgentParams.class, envParams);
	}

	@Override
	protected LearningAgentParams newAgentParam() {
		return new LearningAgentParams();
	}

	private static final long serialVersionUID = 2L;
}
