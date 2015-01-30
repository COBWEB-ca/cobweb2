package org.cobweb.cobweb2.plugins.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.impl.ComplexAgent.BumpAgentCause;
import org.cobweb.cobweb2.impl.ComplexAgent.BumpWallCause;
import org.cobweb.cobweb2.impl.ComplexAgent.EatAgentCause;
import org.cobweb.cobweb2.impl.ComplexAgent.EatFavoriteFoodCause;
import org.cobweb.cobweb2.impl.ComplexAgent.EatFoodCause;
import org.cobweb.cobweb2.impl.ComplexAgent.ReproductionCause;
import org.cobweb.cobweb2.impl.ComplexAgent.SexualReproductionCause;
import org.cobweb.cobweb2.impl.ComplexAgent.StepForwardCause;
import org.cobweb.cobweb2.impl.ComplexAgent.TurnCause;
import org.cobweb.cobweb2.plugins.EnergyMutator;
import org.cobweb.cobweb2.plugins.SpawnMutator;
import org.cobweb.cobweb2.plugins.StatefulMutatorBase;
import org.cobweb.cobweb2.plugins.StepMutator;
import org.cobweb.cobweb2.plugins.pd.PDMutator.PDPunishmentCause;
import org.cobweb.cobweb2.plugins.pd.PDMutator.PDRewardCause;
import org.cobweb.cobweb2.plugins.pd.PDMutator.PDSuckerCause;
import org.cobweb.cobweb2.plugins.pd.PDMutator.PDTemptationCause;


public class StatsMutator extends StatefulMutatorBase<AgentStatistics>
implements EnergyMutator, SpawnMutator, StepMutator {

	private SimulationInternals sim;

	public StatsMutator(SimulationInternals sim) {
		super(AgentStatistics.class);
		this.sim = sim;
	}

	@Override
	public void onEnergyChange(Agent agent, int delta, Cause cause) {
		AgentStatistics stats = getAgentState(agent);
		if (stats == null) // Agent is dead
			return;

		// movement
		if (cause instanceof StepForwardCause) {
			stats.countSteps++;
			stats.energyLossMovement -= delta;
		} else if (cause instanceof TurnCause) {
			stats.countTurns++;
			stats.energyLossMovement -= delta;
		} else if (cause instanceof BumpWallCause) {
			stats.countRockBumps++;
			stats.energyLossMovement -= delta;
		} else if (cause instanceof BumpAgentCause) {
			stats.countAgentBumps++;
			stats.energyLossMovement -= delta;
		}
		// food
		else if (cause instanceof EatFavoriteFoodCause) {
			stats.energyGainFoodMine +=delta;
		} else if (cause instanceof EatFoodCause) {
			stats.energyGainFoodOther += delta;
		} else if (cause instanceof EatAgentCause) {
			stats.energyGainFoodAgents += delta;
		}
		// PD
		else if (cause instanceof PDRewardCause) {
			stats.pdReward++;
		} else if (cause instanceof PDSuckerCause) {
			stats.pdSucker++;
		} else if (cause instanceof PDTemptationCause) {
			stats.pdTemptation++;
		} else if (cause instanceof PDPunishmentCause) {
			stats.pdPunishment++;
		}
		// reproduction
		else if (cause instanceof ReproductionCause) {
			stats.energyLossReproduction -= delta;
			if (cause instanceof SexualReproductionCause)
				stats.sexualPregs++;
		}


	}

	@Override
	public void onStep(Agent agent, Location from, Location to) {
		if (to == null)
			return;

		AgentStatistics stats = getAgentState(agent);
		stats.addPathStep(to);
	}

	@Override
	public void onDeath(Agent agent) {
		AgentStatistics stats = getAgentState(agent);
		// FIXME only happens because environment kills old agents when loading new simulation
		if (stats == null)
			return;
		stats.deathTick = sim.getTime();
		stats.path = null;
	}

	@Override
	public void onSpawn(Agent agent) {
		setAgentState(agent, new AgentStatistics(
				agent,
				sim.getTime()
				));
	}

	@Override
	public void onSpawn(Agent agent, Agent parent) {
		AgentStatistics parentStats = getAgentState(parent);
		parentStats.directChildren++;

		setAgentState(agent, new AgentStatistics(
				agent,
				sim.getTime(),
				parent.id
				));
	}

	@Override
	public void onSpawn(Agent agent, Agent parent1, Agent parent2) {
		AgentStatistics parent1stats = getAgentState(parent1);
		parent1stats.directChildren++;

		AgentStatistics parent2stats = getAgentState(parent2);
		if (parent2stats != null)
			parent2stats.directChildren++;

		setAgentState(agent, new AgentStatistics(
				agent,
				sim.getTime(),
				parent1.id,
				parent2.id
				));
	}

	@Override
	protected void setAgentState(Agent agent, AgentStatistics state) {
		super.setAgentState(agent, state);
		allStats.add(state);
	}

	List<AgentStatistics> allStats = new ArrayList<>();

	public Collection<AgentStatistics> getAllStats() {
		return allStats;
	}

}
