package org.cobweb.cobweb2.plugins.production;

import java.util.Arrays;
import java.util.List;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.StatePluginSource;
import org.cobweb.cobweb2.plugins.PerAgentParams;


public class ProductionParams extends PerAgentParams<ProductionAgentParams> implements StatePluginSource {

	private AgentFoodCountable size;

	public ProductionParams(AgentFoodCountable envParams) {
		super(ProductionAgentParams.class, envParams);
	}

	@Override
	protected ProductionAgentParams newAgentParam() {
		return new ProductionAgentParams(size);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		size = envParams;
		super.resize(envParams);
		for (ProductionAgentParams agentParam : agentParams) {
			agentParam.resize(envParams);
		}
	}

	static final String STATE_NAME_PRODHUNT = "Production Value";

	@Override
	public List<String> getStatePluginKeys() {
		return Arrays.asList(STATE_NAME_PRODHUNT);
	}

	private static final long serialVersionUID = 1L;
}
