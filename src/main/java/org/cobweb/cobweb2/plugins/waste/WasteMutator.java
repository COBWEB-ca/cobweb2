package org.cobweb.cobweb2.plugins.waste;

import java.util.Arrays;
import java.util.Collection;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.cobweb2.plugins.DropManager;
import org.cobweb.cobweb2.plugins.EnergyMutator;
import org.cobweb.cobweb2.plugins.LoggingMutator;
import org.cobweb.cobweb2.plugins.StatefulSpawnMutatorBase;
import org.cobweb.cobweb2.plugins.UpdateMutator;


public class WasteMutator extends StatefulSpawnMutatorBase<WasteState> implements EnergyMutator, UpdateMutator, LoggingMutator,
DropManager<Waste>{

	private WasteParams params;
	private Environment environment;
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
		if (!state.agentParams.wasteMode)
			return;

		if (delta > 0)
			state.energyGained += delta;
		else if (delta < 0)
			state.energyLost -= delta;
	}

	@Override
	public void onUpdate(Agent agent) {
		WasteState state = getAgentState(agent);
		if (!agent.isAlive() || !state.agentParams.wasteMode)
			return;

		WasteAgentParams agentParams = state.agentParams;

		if (agentParams.wasteLimitGain.getValue() > 0 && state.energyGained >= agentParams.wasteLimitGain.getValue()) {
			if (tryPoop(agent, agentParams)) {
				state.energyGained -= agentParams.wasteLimitGain.getValue();
			}
		} else if (agentParams.wasteLimitLoss.getValue() > 0 && state.energyLost >= agentParams.wasteLimitLoss.getValue()) {
			if (tryPoop(agent, agentParams)) {
				state.energyLost -= agentParams.wasteLimitLoss.getValue();
			}
		}
	}

	@Override
	public WasteState stateForNewAgent(Agent agent) {
		WasteAgentParams agentParams = params.agentParams[agent.getType()];

		return new WasteState(agentParams.clone());
	}

	@Override
	protected WasteState stateFromParent(Agent agent, WasteState parentState) {
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

		Waste waste = new Waste(loc, agentParams.wasteInit.getValue(), agentParams.wasteDecay.getValue(), this, agent.getType());
		environment.addDrop(loc, waste);
		return true;
	}

	@Override
	protected boolean validState(WasteState value) {
		return value != null;
	}

	@Override
	public void remove(Waste waste) {
		environment.removeDrop(waste.location);
	}

	public int countTotalWaste()
	{
		int count = 0;
		int map_width = environment.topology.width;
		int map_height = environment.topology.height;

		for(int x = 0; x < map_width; x++)
		{
			for(int y = 0; y < map_width; y++)
			{
				Location loc = new Location(x, y);

				if(environment.hasDrop(loc))
				{
					Drop drop = environment.getDrop(loc);

					if(drop instanceof Waste)
						count++;
				}
			}
		}

		return count;
	}

	public int countAgentWaste(int agentType)
	{
		int count = 0;
		int map_width = environment.topology.width;
		int map_height = environment.topology.height;

		for(int x = 0; x < map_width; x++)
		{
			for(int y = 0; y < map_width; y++)
			{
				Location loc = new Location(x, y);

				if(environment.hasDrop(loc))
				{
					Drop drop = environment.getDrop(loc);

					if(drop instanceof Waste && drop.getProducerType() == agentType)
						count++;
				}
			}
		}

		return count;
	}

	@Override
	public Collection<String> logDataAgent(int agentType) {
		return Arrays.asList(Integer.toString(countAgentWaste(agentType)));
	}

	@Override
	public Collection<String> logDataTotal() {
		return Arrays.asList(Integer.toString(countTotalWaste()));
	}

	@Override
	public Collection<String> logHeadersAgent() {
		return Arrays.asList("Waste");
	}

	@Override
	public Collection<String> logHeaderTotal() {
		return Arrays.asList("Waste");
	}
}
