package org.cobweb.cobweb2.plugins.waste;

import java.util.Collection;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.plugins.EnergyMutator;
import org.cobweb.cobweb2.plugins.StatefulSpawnMutatorBase;
import org.cobweb.cobweb2.plugins.UpdateMutator;


public class WasteMutator extends StatefulSpawnMutatorBase<WasteMutator.WasteState> implements EnergyMutator, UpdateMutator {

	private WasteParams params;
	private Environment environment;
	private SimulationInternals sim;

	public WasteMutator(SimulationInternals sim) {
		super(WasteMutator.WasteState.class, sim);
		this.sim = sim;
	}

	public void setParams(WasteParams wasteParams) {
		this.params = wasteParams;
	}

	public void initEnvironment(Environment env, boolean keepOldWaste) {
		this.environment = env;
		if (!keepOldWaste)
			clearAgentStates();
	}

	@Override
	public void onEnergyChange(Agent agent, int delta) {
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
		if (state == null)
			return;

		state.update();
	}

	protected class WasteState {
		public int energyLost;
		public int energyGained;
		private final WasteAgentParams agentParams;
		private final Agent agent;

		public WasteState(Agent agent, WasteAgentParams wasteAgentParams) {
			this.agent = agent;
			this.agentParams = wasteAgentParams;
		}

		private boolean tryPoop() {
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

		public void update() {
			if (agentParams.wasteLimitGain > 0 && energyGained >= agentParams.wasteLimitGain) {
				if (tryPoop()) {
					energyGained -= agentParams.wasteLimitGain;
				}
			} else if (agentParams.wasteLimitLoss > 0 && energyLost >= agentParams.wasteLimitLoss) {
				if (tryPoop()) {
					energyLost -= agentParams.wasteLimitLoss;
				}
			}
		}
	}

	@Override
	public WasteState stateForNewAgent(Agent agent) {
		WasteAgentParams agentParams = params.agentParams[agent.getType()];
		if (!agentParams.wasteMode)
			return null; // Don't need state when waste disabled

		return new WasteState(agent, agentParams.clone());
	}

	@Override
	protected WasteState stateFromParent(Agent agent, WasteState parentState) {
		if (parentState == null)
			return null;
		return new WasteState(agent, parentState.agentParams.clone());
	}

}
