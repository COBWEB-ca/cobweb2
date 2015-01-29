package org.cobweb.cobweb2.plugins.waste;

import java.util.Collection;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.plugins.AgentState;

class WasteState implements AgentState {

	private final WasteMutator wasteMutator;
	public int energyLost;
	public int energyGained;
	final WasteAgentParams agentParams;
	private final Agent agent;

	public WasteState(WasteMutator wasteMutator, Agent agent, WasteAgentParams wasteAgentParams) {
		this.wasteMutator = wasteMutator;
		this.agent = agent;
		this.agentParams = wasteAgentParams;
	}

	private boolean tryPoop() {
		Collection<Location> target = this.wasteMutator.environment.getNearLocations(agent.getPosition());
		Location loc = null;
		boolean replaceFood = false;
		for (Location l : target) {
			if (this.wasteMutator.environment.hasDrop(l) || this.wasteMutator.environment.hasAgent(l))
				continue;

			if (!this.wasteMutator.environment.hasAnythingAt(l)) {
				// empty space, drop it here
				loc = l;
				replaceFood = false;
				break;
			}
			if (!replaceFood && this.wasteMutator.environment.hasFood(l)) {
				// space with food, note it down, see if anything else is totally empty
				loc = l;
				replaceFood = true;
			}
		}

		if (loc == null)
			return false;

		if (replaceFood)
			this.wasteMutator.environment.removeFood(loc);

		Waste waste = new Waste(this.wasteMutator.sim.getTime(), agentParams.wasteInit, agentParams.wasteDecay);
		this.wasteMutator.environment.addDrop(loc, waste);
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

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 1L;
}