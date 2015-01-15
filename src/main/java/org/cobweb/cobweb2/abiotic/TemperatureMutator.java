package org.cobweb.cobweb2.abiotic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.StateParameter;
import org.cobweb.cobweb2.core.StatePlugin;
import org.cobweb.cobweb2.core.params.AgentFoodCountable;
import org.cobweb.cobweb2.interconnect.SpawnMutator;
import org.cobweb.cobweb2.interconnect.StepMutator;

/**
 * TemperatureMutator is an instance of Step and Spawn Mutator
 *
 * @author ???
 */
public class TemperatureMutator implements StepMutator, SpawnMutator, StatePlugin {

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
	private float locToPenalty(Location l, TemperatureAgentParams aPar) {
		float temp = getTemp(l);
		float ptemp = aPar.preferedTemp;
		float diff = Math.abs(temp - ptemp);
		diff = Math.max(diff - aPar.preferedTempRange, 0);
		float f = diff * aPar.differenceFactor;
		return f;
	}

	@Override
	public Collection<String> logDataAgent(int agentType) {
		return NO_DATA;
	}

	@Override
	public Collection<String> logDataTotal() {
		return NO_DATA;
	}

	@Override
	public Collection<String> logHeadersAgent() {
		return NO_DATA;
	}

	@Override
	public Collection<String> logHeaderTotal() {
		return NO_DATA;
	}

	@Override
	public void onDeath(Agent agent) {
		// Nothing
	}

	@Override
	public void onSpawn(Agent agent) {
		TemperatureAgentParams aPar = params.agentParams[agent.getType()];

		float f = locToPenalty(agent.getPosition(), aPar);
		if (aPar.parameter.field != null)
			aPar.parameter.modifyValue(agent, 1, f);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent) {
		onSpawn(agent);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent1, Agent parent2) {
		onSpawn(agent);
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

		float toFactor = locToPenalty(to, aPar);
		float fromFactor = locToPenalty(from, aPar);

		float delta = toFactor - fromFactor;

		if (Math.abs(delta) < 1e-10 && aPar.parameter.field != null) {
			aPar.parameter.modifyValue(agent, 1, delta);
		}
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
			double value = locToPenalty(agent.getPosition(), params.agentParams[agent.getType()]);
			return value;
		}

	}

}
