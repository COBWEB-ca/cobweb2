package org.cobweb.cobweb2.plugins.toxin;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;


public class ToxinParams extends PerAgentParams<ToxinAgentParams> {

	private AgentFoodCountable size;

	public ToxinParams(AgentFoodCountable initialSize) {
		super(ToxinAgentParams.class, initialSize);
	}

	@Override
	protected ToxinAgentParams newAgentParam() {
		return new ToxinAgentParams(size);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		this.size = envParams;
		super.resize(envParams);

		for (ToxinAgentParams agentParam : agentParams) {
			agentParam.resize(envParams);
		}
	}

	private static final long serialVersionUID = 1L;
}
