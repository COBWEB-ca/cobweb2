package org.cobweb.cobweb2.plugins.disease;

import org.cobweb.cobweb2.impl.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;

public class DiseaseParams extends PerAgentParams<DiseaseAgentParams> {

	private AgentFoodCountable size;

	public DiseaseParams(AgentFoodCountable size) {
		super(DiseaseAgentParams.class, size);
	}

	@Override
	protected DiseaseAgentParams newAgentParam() {
		return new DiseaseAgentParams(size);
	}

	@Override
	public void resize(AgentFoodCountable newSize) {
		this.size = newSize;
		super.resize(newSize);

		for(DiseaseAgentParams p : agentParams) {
			p.resize(this.size);
		}
	}

	private static final long serialVersionUID = 2L;
}
