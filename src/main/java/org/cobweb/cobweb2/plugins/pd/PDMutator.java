package org.cobweb.cobweb2.plugins.pd;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.ContactMutator;
import org.cobweb.cobweb2.plugins.StatefulSpawnMutatorBase;


public class PDMutator extends StatefulSpawnMutatorBase<PDState> implements ContactMutator {

	SimulationInternals sim;
	PDParams params;

	public PDMutator(SimulationInternals sim) {
		super(PDState.class, sim);
		this.sim = sim;
	}

	public void setParams(PDParams pdParams) {
		this.params = pdParams;
	}

	@Override
	public PDState stateForNewAgent(Agent agent) {
		if (!params.enable)
			return null;

		return new PDState(this, (ComplexAgent) agent,
				params.agentParams[agent.getType()].clone());
	}

	@Override
	protected PDState stateFromParent(Agent agent, PDState parentState) {
		if (!params.enable)
			return null;

		return new PDState(this, (ComplexAgent) agent,
				parentState.agentParams.clone());
	}

	@Override
	public void onContact(Agent bumper, Agent bumpee) {
		PDState thisPD = getAgentState(bumper);
		if (thisPD == null)
			return;

		thisPD.onStepBumpAgent((ComplexAgent) bumpee);
	}


}
