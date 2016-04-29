package org.cobweb.cobweb2.plugins.learning;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;


public class LearningParams extends PerAgentParams<LearningAgentParams> {

	public LearningParams(AgentFoodCountable initialSize) {
		super(LearningAgentParams.class, initialSize);
	}

	@Override
	protected LearningAgentParams newAgentParam() {
		return new LearningAgentParams();
	}

	private static final long serialVersionUID = 1L;
}
