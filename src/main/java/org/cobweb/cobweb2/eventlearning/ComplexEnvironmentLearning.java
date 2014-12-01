package org.cobweb.cobweb2.eventlearning;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.core.AgentSpawner;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.cobweb2.production.ProductionParams;


public class ComplexEnvironmentLearning extends ComplexEnvironment {

	public ComplexEnvironmentLearning() {
		super();
	}

	private LearningAgentParams learningData[];

	@Override
	protected void copyParamsFromParser(SimulationConfig p) {
		super.copyParamsFromParser(p);
		learningData = p.getLearningParams().getLearningAgentParams();
	}

	@Override
	public void setDefaultMutableAgentParam() {
		ComplexAgentLearning.setDefaultMutableParams(agentData, learningData, prodData);
	}

	@Override
	protected void spawnAgent(Location location, int agentType) {
		ComplexAgentLearning child = (ComplexAgentLearning)AgentSpawner.spawn();
		child.init(agentType, location, (ComplexAgentParams) agentData[agentType].clone(),
				(ProductionParams)prodData[agentType].clone(),
				(LearningAgentParams)learningData[agentType].clone()); // Default
	}
}
