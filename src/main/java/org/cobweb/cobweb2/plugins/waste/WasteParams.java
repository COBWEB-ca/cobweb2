package org.cobweb.cobweb2.plugins.waste;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;


public class WasteParams extends PerAgentParams<WasteAgentParams> {

	public WasteParams(AgentFoodCountable size) {
		super(WasteAgentParams.class, size);
	}

	@Override
	protected WasteAgentParams newAgentParam() {
		return new WasteAgentParams();
	}

	private static final long serialVersionUID = 2L;
}
