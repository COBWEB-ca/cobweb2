package org.cobweb.cobweb2.plugins.abiotic;

import java.util.ArrayList;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.cobweb2.core.StateParameter;
import org.cobweb.cobweb2.core.StatePlugin;
import org.cobweb.cobweb2.plugins.EnvironmentMutator;
import org.cobweb.cobweb2.plugins.StatefulMutatorBase;
import org.cobweb.cobweb2.plugins.StepMutator;

/**
 * AbioticMutator is an instance of Step and Spawn Mutator
 *
 * @author ???
 */
public class AbioticMutator extends StatefulMutatorBase<AbioticState> implements StepMutator, StatePlugin, EnvironmentMutator {

	public AbioticMutator() {
		super(AbioticState.class);
	}

	public AbioticParams params;

	private SimulationTimeSpace sim;

	/**
	 * @param loc location.
	 * @return The abiotic factor value at location
	 */
	public float getValue(int factor, Location loc) {
		float x = (float) loc.x / sim.getTopology().width;
		float y = (float) loc.y / sim.getTopology().height;

		AbioticFactor abioticFactor = params.factors.get(factor);
		float value = abioticFactor.getValue(x, y);
		return value;
	}

	/**
	 * @param l agent location.
	 * @param aPar Agent type specific parameters.
	 * @return The effect the abiotic factor has on the agent. 0 if agent is unaffected.
	 */
	private float effectAtLocation(int factor, Location l, AbioticAgentParams aPar) {
		float temp = getValue(factor, l);
		AbioticPreferenceParam agentFactorParams = aPar.factorParams[factor].preference;

		return agentFactorParams.score(temp);
	}

	/**
	 * During a step
	 *
	 * @param agent The agent doing the step
	 * @param from Location the agent is moving from.
	 * @param to Location the agent is moving to.
	 */
	@Override
	public void onStep(Agent agent, Location from, Location to) {
		if (to == null) {
			removeAgentState(agent);
			return;
		}

		AbioticAgentParams aPar = params.agentParams[agent.getType()];
		AbioticState state = getAgentState(agent);

		if (from == null) {
			state = new AbioticState(aPar);
			for (int i = 0; i < params.factors.size(); i++) {
				state.factorStates[i].originalParamValue = aPar.factorParams[i].parameter.getValue(agent);
			}
			setAgentState(agent, state);
		}

		for (int i = 0; i < params.factors.size(); i++) {
			AbioticFactorState factorState = state.factorStates[i];
			float effect = effectAtLocation(i, to, aPar);

			if (from != null && effectAtLocation(i, from, aPar) == effect)
				continue;

			float multiplier = 1 + effect;
			float newValue = factorState.originalParamValue * multiplier;

			factorState.agentParams.parameter.setValue(agent, newValue);
		}
	}

	/**
	 * Sets the parameters according to the simulation configuration.
	 * @param sim simulation sim
	 * @param params abiotic parameters from the simulation configuration.
	 */
	public void setParams(SimulationTimeSpace sim, AbioticParams params) {
		this.params = params;
		this.sim = sim;
	}

	@Override
	public List<StateParameter> getParameters() {
		List<StateParameter> res = new ArrayList<>(params.factors.size());
		for (int i = 0; i < params.factors.size(); i++) {
			res.add(new AbioticStatePenalty(i));
		}
		return res;
	}

	/**
	 * Magnitude of abiotic discomfort the agent is experiencing
	 */
	private class AbioticStatePenalty implements StateParameter {

		private int factor;

		public AbioticStatePenalty(int factor) {
			this.factor = factor;
		}

		@Override
		public String getName() {
			return String.format(AbioticParams.STATE_NAME_ABIOTIC_PENALTY, factor + 1);
		}

		@Override
		public double getValue(Agent agent) {
			AbioticAgentParams aPar = params.agentParams[agent.getType()];
			double value = Math.abs(effectAtLocation(factor, agent.getPosition(), aPar));
			return value;
		}

	}

	@Override
	public void update() {
		for (AbioticFactor f : params.factors) {
			f.update(sim);
		}
	}

	@Override
	public void loadNew() {
		// nothing
	}

	@Override
	protected boolean validState(AbioticState value) {
		return value.factorStates.length == this.params.factors.size();
	}

}
