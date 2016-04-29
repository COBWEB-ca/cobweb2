package org.cobweb.cobweb2.plugins.waste;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;


public class WasteParams extends PerAgentParams<WasteAgentParams> {

	private AgentFoodCountable size;

	public WasteParams(AgentFoodCountable size) {
		super(WasteAgentParams.class, size);
		this.size = size;
	}

	@Override
	protected WasteAgentParams newAgentParam() {
		return new WasteAgentParams(size);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		size = envParams;
		super.resize(envParams);
		for (WasteAgentParams agentParam : agentParams) {
			agentParam.resize(envParams);
		}
	}

	private static final long serialVersionUID = 2L;
}
