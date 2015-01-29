package org.cobweb.cobweb2.plugins.waste;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.plugins.EnergyMutator;
import org.cobweb.cobweb2.plugins.StatefulSpawnMutatorBase;
import org.cobweb.cobweb2.plugins.UpdateMutator;


public class WasteMutator extends StatefulSpawnMutatorBase<WasteState> implements EnergyMutator, UpdateMutator {

	private WasteParams params;
	Environment environment;
	SimulationInternals sim;

	public WasteMutator(SimulationInternals sim) {
		super(WasteState.class, sim);
		this.sim = sim;
	}

	public void setParams(WasteParams wasteParams) {
		this.params = wasteParams;
	}

	public void initEnvironment(Environment env) {
		this.environment = env;
	}

	@Override
	public void onEnergyChange(Agent agent, int delta, Cause cause) {
		WasteState state = getAgentState(agent);
		if (state == null)
			return;

		if (delta > 0)
			state.energyGained += delta;
		else if (delta < 0)
			state.energyLost -= delta;
	}

	@Override
	public void onUpdate(Agent agent) {
		WasteState state = getAgentState(agent);
		if (state == null || !agent.isAlive())
			return;

		state.update();
	}

	@Override
	public WasteState stateForNewAgent(Agent agent) {
		WasteAgentParams agentParams = params.agentParams[agent.getType()];
		if (!agentParams.wasteMode)
			return null; // Don't need state when waste disabled

		return new WasteState(this, agent, agentParams.clone());
	}

	@Override
	protected WasteState stateFromParent(Agent agent, WasteState parentState) {
		if (parentState == null)
			return null;
		return new WasteState(this, agent, parentState.agentParams.clone());
	}

}
