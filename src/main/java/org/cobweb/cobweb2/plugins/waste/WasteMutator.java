package org.cobweb.cobweb2.plugins.waste;

import java.util.Collection;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.cobweb2.plugins.EnergyMutator;
import org.cobweb.cobweb2.plugins.StatefulSpawnMutatorBase;
import org.cobweb.cobweb2.plugins.UpdateMutator;


public class WasteMutator extends StatefulSpawnMutatorBase<WasteState> implements EnergyMutator, UpdateMutator {

	private WasteParams params;
	Environment environment;
	SimulationTimeSpace sim;

	public WasteMutator(SimulationTimeSpace sim) {
		super(WasteState.class, sim);
		this.sim = sim;
	}

	public void setParams(WasteParams wasteParams, Environment env) {
		this.params = wasteParams;
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

		WasteAgentParams agentParams = state.agentParams;

		if (agentParams.wasteLimitGain > 0 && state.energyGained >= agentParams.wasteLimitGain) {
			if (tryPoop(agent, agentParams)) {
				state.energyGained -= agentParams.wasteLimitGain;
			}
		} else if (agentParams.wasteLimitLoss > 0 && state.energyLost >= agentParams.wasteLimitLoss) {
			if (tryPoop(agent, agentParams)) {
				state.energyLost -= agentParams.wasteLimitLoss;
			}
		}
	}

	@Override
	public WasteState stateForNewAgent(Agent agent) {
		WasteAgentParams agentParams = params.agentParams[agent.getType()];
		if (!agentParams.wasteMode)
			return null; // Don't need state when waste disabled

		return new WasteState(agentParams.clone());
	}

	@Override
	protected WasteState stateFromParent(Agent agent, WasteState parentState) {
		if (parentState == null)
			return null;
		return new WasteState(parentState.agentParams.clone());
	}

	private boolean tryPoop(Agent agent, WasteAgentParams agentParams) {
		Collection<Location> target = environment.getNearLocations(agent.getPosition());
		Location loc = null;
		boolean replaceFood = false;
		for (Location l : target) {
			if (environment.hasDrop(l) || environment.hasAgent(l))
				continue;

			if (!environment.hasAnythingAt(l)) {
				// empty space, drop it here
				loc = l;
				replaceFood = false;
				break;
			}
			if (!replaceFood && environment.hasFood(l)) {
				// space with food, note it down, see if anything else is totally empty
				loc = l;
				replaceFood = true;
			}
		}

		if (loc == null)
			return false;

		if (replaceFood)
			environment.removeFood(loc);

		Waste waste = new Waste(sim.getTime(), agentParams.wasteInit, agentParams.wasteDecay);
		environment.addDrop(loc, waste);
		return true;
	}

	@Override
	protected boolean validState(WasteState value) {
		return value != null;
	}
}
