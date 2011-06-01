package cwcore;

import learning.LearningAgentParams;
import cwcore.complexParams.ComplexAgentParams;
import driver.SimulationConfig;


public class ComplexEnvironmentLearning extends ComplexEnvironment {

	public ComplexEnvironmentLearning() {
		// TODO Auto-generated constructor stub
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
	protected void spawnAgent(int action, Location location, int agentType) {
		ComplexAgentLearning child = (ComplexAgentLearning)AgentSpawner.spawn();
		child.init(agentType, location, action, (ComplexAgentParams) agentData[agentType].clone(),
				(LearningAgentParams)learningData[agentType].clone()); // Default
	}
}
