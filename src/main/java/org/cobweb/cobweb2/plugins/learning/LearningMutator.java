package org.cobweb.cobweb2.plugins.learning;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.RandomSource;
import org.cobweb.cobweb2.plugins.StatefulSpawnMutatorBase;


public class LearningMutator extends StatefulSpawnMutatorBase<LearningState> {

	private LearningParams params;

	protected LearningMutator(RandomSource rand, LearningParams params) {
		super(LearningState.class, rand);
		this.params = params;
	}

	@Override
	protected LearningState stateForNewAgent(Agent agent) {
		LearningAgentParams typeParams = params.agentParams[agent.getType()];
		return new LearningState(typeParams.clone());
	}

	@Override
	protected LearningState stateFromParent(Agent agent, LearningState parentState) {
		return new LearningState(parentState.agentParams.clone());
	}

	@Override
	protected boolean validState(LearningState value) {
		return value != null;
	}

}
