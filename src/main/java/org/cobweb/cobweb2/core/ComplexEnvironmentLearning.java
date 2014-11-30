package org.cobweb.cobweb2.core;

import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.cobweb2.core.params.ProductionParams;
import org.cobweb.cobweb2.eventlearning.LearningAgentParams;
import org.cobweb.cobweb2.ui.swing.SimulationConfig;


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
