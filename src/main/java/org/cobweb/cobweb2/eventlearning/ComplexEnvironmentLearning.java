package org.cobweb.cobweb2.eventlearning;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;


public class ComplexEnvironmentLearning extends ComplexEnvironment {

	public ComplexEnvironmentLearning(SimulationInternals simulation) {
		super(simulation);
	}

	private LearningAgentParams learningData[];

	@Override
	protected void copyParamsFromParser(SimulationConfig p) {
		super.copyParamsFromParser(p);
		learningData = p.getLearningParams().getLearningAgentParams();
	}

	@Override
	public void setDefaultMutableAgentParam() {
		ComplexAgentLearning.setDefaultMutableParams(agentData, learningData);
	}

	@Override
	protected void spawnAgent(LocationDirection location, int agentType) {
		ComplexAgentLearning child = (ComplexAgentLearning)simulation.newAgent();
		child.init(this, agentType, location, (ComplexAgentParams) agentData[agentType].clone(),
				(LearningAgentParams)learningData[agentType].clone()); // Default
	}
}
