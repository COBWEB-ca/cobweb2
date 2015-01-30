package org.cobweb.cobweb2.plugins.abiotic;

import java.util.Arrays;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.StateParameter;
import org.cobweb.cobweb2.core.StatePlugin;
import org.cobweb.cobweb2.plugins.StatefulMutatorBase;
import org.cobweb.cobweb2.plugins.StepMutator;

/**
 * TemperatureMutator is an instance of Step and Spawn Mutator
 *
 * @author ???
 */
public class TemperatureMutator extends StatefulMutatorBase<TemperatureState> implements StepMutator, StatePlugin {

	public TemperatureMutator() {
		super(TemperatureState.class);
	}

	private TemperatureParams params;

	private int bandNumber;

	/**
	 * Height of the simulation grid.
	 */
	private int height = -9000;

	/**
	 * @param loc Scrutinized location.
	 * @return The temperature of the location within the temperature band.
	 */
	private float getTemp(Location loc) {
		int lat = loc.y * bandNumber / height;
		assert (lat < params.tempBands.length);
		return params.tempBands[lat];
	}

	/**
	 * @param l Scrutinized location.
	 * @param aPar Agent type specific temperature parameters.
	 * @return The effect temperature has on the agent.  0 if agent is unaffected.
	 */
	private float effectAtLocation(Location l, TemperatureAgentParams aPar) {
		float temp = getTemp(l);
		float ptemp = aPar.preferedTemp;
		float diff = Math.abs(temp - ptemp);
		diff = Math.max(diff - aPar.preferedTempRange, 0);
		float res = diff * aPar.differenceFactor;
		return res;
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
		TemperatureAgentParams aPar = params.agentParams[agent.getType()];
		TemperatureState state = getAgentState(agent);

		if (from == null) {
			state = new TemperatureState(aPar);
			state.originalParamValue = aPar.parameter.getValue(agent);
			setAgentState(agent, state);
		}

		if (to == null) {
			removeAgentState(agent);
			return;
		}

		float effect = effectAtLocation(to, aPar);

		if (from != null && effectAtLocation(from, aPar) == effect)
			return;

		float multiplier = 1 + effect;
		float newValue = state.originalParamValue * multiplier;

		aPar.parameter.setValue(agent, newValue);
	}

	/**
	 * Sets the temperature parameters according to the simulation configuration.
	 *
	 * @param params Temperature parameters from the simulation configuration.
	 * @param env Environment parameters from the simulation configuration.
	 */
	public void setParams(TemperatureParams params, AgentFoodCountable env) {
		this.params = params;
		height = env.getHeight();
		bandNumber = Math.min(TemperatureParams.TEMPERATURE_BANDS, height);
	}

	@Override
	public List<StateParameter> getParameters() {
		return Arrays.asList(
				(StateParameter)new AbioticStatePenalty());
	}

	/**
	 * Magnitude of abiotic discomfort the agent is experiencing
	 */
	private class AbioticStatePenalty implements StateParameter {

		@Override
		public String getName() {
			return TemperatureParams.STATE_NAME_ABIOTIC_PENALTY;
		}

		@Override
		public double getValue(Agent agent) {
			TemperatureAgentParams aPar = params.agentParams[agent.getType()];
			double value = Math.abs(effectAtLocation(agent.getPosition(), aPar));
			return value;
		}

	}

}
