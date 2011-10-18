package cwcore;

import learning.LearningAgentParams;
import cwcore.complexParams.ComplexAgentParams;
import driver.SimulationConfig;


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
		ComplexAgentLearning.setDefaultMutableParams(agentData, learningData);
	}

	@Override
	protected void spawnAgent(Location location, int agentType) {
		ComplexAgentLearning child = (ComplexAgentLearning)AgentSpawner.spawn();
		child.init(agentType, location, (ComplexAgentParams) agentData[agentType].clone(),
				(LearningAgentParams)learningData[agentType].clone()); // Default
	}
}
