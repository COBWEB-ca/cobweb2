package org.cobweb.cobweb2.plugins.vision;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.StatefulMutatorBase;
import org.cobweb.cobweb2.plugins.StepMutator;


public class VisionMutator extends StatefulMutatorBase<VisionState> implements StepMutator {

	public VisionMutator() {
		super(VisionState.class);
	}

	@Override
	public void onStep(Agent agent, Location from, Location to) {
		if (from == null && to != null) {
			setAgentState(agent, new VisionState(((ComplexAgent)agent).environment, agent));
		}
	}

	@Override
	protected boolean validState(VisionState value) {
		return false;
	}

}
