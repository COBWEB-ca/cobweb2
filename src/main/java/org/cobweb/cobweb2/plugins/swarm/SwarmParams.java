package org.cobweb.cobweb2.plugins.swarm;

import java.util.ArrayList;
import java.util.Collection;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.StatePluginSource;
import org.cobweb.cobweb2.plugins.PerAgentParams;


public class SwarmParams extends PerAgentParams<SwarmAgentParams> implements StatePluginSource {

	private AgentFoodCountable size;

	public SwarmParams(AgentFoodCountable size) {
		super(SwarmAgentParams.class);
		this.size = size;
		resize(size);
	}


	@Override
	protected SwarmAgentParams newAgentParam() {
		return new SwarmAgentParams(size);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		super.resize(envParams);

		for (int i =0; i < agentParams.length; i++) {
			agentParams[i].resize(envParams);
		}
	}

	static final String STATE_NAME_SWARM_BENEFIT = "Swarm %d Benefit";
	@Override
	public Collection<String> getStatePluginKeys() {
		Collection<String> result = new ArrayList<>();
		for (int i = 0; i < size.getAgentTypes(); i++) {
			result.add(String.format(STATE_NAME_SWARM_BENEFIT, i + 1));
		}
		return result;
	}

	private static final long serialVersionUID = 1L;
}
