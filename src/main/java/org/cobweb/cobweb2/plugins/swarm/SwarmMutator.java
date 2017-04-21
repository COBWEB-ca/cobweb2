package org.cobweb.cobweb2.plugins.swarm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.cobweb2.core.StateParameter;
import org.cobweb.cobweb2.core.StatePlugin;
import org.cobweb.cobweb2.plugins.SpawnMutator;
import org.cobweb.cobweb2.plugins.StatefulMutatorBase;
import org.cobweb.cobweb2.plugins.UpdateMutator;


public class SwarmMutator extends StatefulMutatorBase<SwarmState> implements UpdateMutator, StatePlugin, SpawnMutator {

	public SwarmMutator() {
		super(SwarmState.class);
	}

	public SwarmParams params;
	private SimulationTimeSpace sim;
	private Environment env;

	/**
	 * Sets the parameters according to the simulation configuration.
	 * @param sim simulation sim
	 * @param params swarm parameters from the simulation configuration.
	 */
	public void setParams(SimulationTimeSpace sim, SwarmParams params, Environment env) {
		this.params = params;
		this.sim = sim;
		this.env = env;

		causeKeys = new CauseKey[params.agentParams.length];
		for (int i = 0 ; i < causeKeys.length; i++) {
			causeKeys[i] = new CauseKey(i);
		}
	}

	private CauseKey[] causeKeys;
	private class CauseKey {
		private int index;
		public CauseKey(int index) {
			this.index = index;
		}

		@Override
		public String toString() {
			return SwarmMutator.this.toString() + ".agent[" + index + "]";
		}
	}

	@Override
	public void onDeath(Agent agent) {
		// nothing
	}

	@Override
	public void onSpawn(Agent agent) {
		SwarmAgentParams aPar = params.agentParams[agent.getType()].clone();
		setAgentState(agent, new SwarmState(aPar));
	}

	@Override
	public void onSpawn(Agent agent, Agent parent) {
		setAgentState(agent, getAgentState(parent).clone());
	}

	@Override
	public void onSpawn(Agent agent, Agent parent1, Agent parent2) {
		Agent parent = sim.getRandom().nextBoolean() ? parent1 : parent2;
		onSpawn(agent, parent);
	}

	@Override
	public List<StateParameter> getParameters() {
		List<StateParameter> res = new ArrayList<>(params.agentParams.length);
		for (int i = 0; i < params.agentParams.length; i++) {
			res.add(new SwarmStateBenefit(i));
		}
		return res;
	}

	private class SwarmStateBenefit implements StateParameter {

		private int type;

		public SwarmStateBenefit(int type) {
			this.type = type;
		}

		@Override
		public String getName() {
			return String.format(SwarmParams.STATE_NAME_SWARM_BENEFIT, type + 1);
		}

		@Override
		public double getValue(Agent agent) {
			SwarmState state = getAgentState(agent);
			PairwiseEffect effect = state.agentParams.effects[type];
			float score = calculateScore(effect, type, agent);
			float relScore = effect.relativeScore(score);
			return relScore;
		}
	}

	@Override
	public void onUpdate(Agent agent) {
		SwarmState state = getAgentState(agent);
		if (!agent.isAlive())
			return;

		for (int i = 0; i < state.agentParams.effects.length; i++) {
			PairwiseEffect effect = state.agentParams.effects[i];
			float score = calculateScore(effect, i, agent);
			effect.parameter.modifyValue(causeKeys[i], agent, score);
		}
	}

	private float calculateScore(PairwiseEffect effect, int type, Agent agent) {
		if (effect.radius <= 0)
			return 1;

		Collection<Location> locations = sim.getTopology().getArea(agent.getPosition(), effect.radius);
		int count = 0;
		for (Location location : locations) {
			if (location.equals(agent.getPosition()))
				continue;

			Agent neighbor = env.getAgent(location);
			if (neighbor != null && neighbor.getType() == type)
				count++;
		}

		return effect.score(count);
	}

	@Override
	protected boolean validState(SwarmState value) {
		return value.agentParams.effects.length == this.params.agentParams.length;
	}

}
