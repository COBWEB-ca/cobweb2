package cwcore;

import learning.LearningAgentParams;
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

}
