package org.cobweb.cobweb2.impl;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;


public class AgentParams extends PerAgentParams<ComplexAgentParams> {

	private AgentFoodCountable size;

	public AgentParams(AgentFoodCountable size) {
		super(ComplexAgentParams.class);
		this.size = size;
		resize(size);
	}

	@Override
	protected ComplexAgentParams newAgentParam() {
		return new ComplexAgentParams(size);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		size = envParams;
		super.resize(size);

		for (ComplexAgentParams complexAgentParams : agentParams) {
			complexAgentParams.resize(size);
		}
	}

	private static final long serialVersionUID = 2L;
}
