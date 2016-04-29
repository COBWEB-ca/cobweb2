package org.cobweb.cobweb2.plugins.learning;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.ControllerInput;
import org.cobweb.cobweb2.core.RandomSource;
import org.cobweb.cobweb2.plugins.ControllerInputMutator;
import org.cobweb.cobweb2.plugins.EnergyMutator;
import org.cobweb.cobweb2.plugins.StatefulSpawnMutatorBase;
import org.cobweb.cobweb2.plugins.UpdateMutator;


public class LearningMutator extends StatefulSpawnMutatorBase<LearningState>
implements EnergyMutator, ControllerInputMutator, UpdateMutator {

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

	@Override
	public void onEnergyChange(Agent agent, int delta, Cause cause) {
		LearningState state = getAgentState(agent);
		state.recordChange(delta, cause);
	}

	@Override
	public void onControl(Agent agent, ControllerInput cInput) {
		LearningState state = getAgentState(agent);
		state.recordInput(cInput);
	}

	@Override
	public void onUpdate(Agent agent) {
		LearningState state = getAgentState(agent);
		state.update();
	}

}
