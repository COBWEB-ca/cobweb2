package org.cobweb.cobweb2.abiotic;

import java.util.Collection;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.interconnect.SpawnMutator;
import org.cobweb.cobweb2.interconnect.StepMutator;
import org.cobweb.util.ReflectionUtil;

/**
 * TemperatureMutator is an instance of Step and Spawn Mutator
 *
 * @author ???
 */
public class TemperatureMutator implements StepMutator, SpawnMutator {

	private TemperatureParams params;

	private int bandNumber;

	/**
	 * Height of the simulation grid.
	 */
	private int height = -9000;

	/**
	 * TemperatureMutator is an instance of Step and Spawn Mutator.
	 */
	public TemperatureMutator() {
		// Nothing
	}

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
	public void onDeath(ComplexAgent agent) {
		// Nothing
	}

	@Override
	public void onSpawn(ComplexAgent agent) {
		TemperatureAgentParams aPar = params.agentParams[agent.type()];

		float f = locToPenalty(agent.getPosition(), aPar);
		if (aPar.parameter.field != null)
			ReflectionUtil.addField(agent.params, aPar.parameter.field, f);
	}

	@Override
	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		onSpawn(agent);
	}

	@Override
	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2) {
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
	public void onStep(ComplexAgent agent, Location from, Location to) {
		TemperatureAgentParams aPar = params.agentParams[agent.type()];

		float toFactor = locToPenalty(to, aPar);
		float fromFactor = locToPenalty(from, aPar);

		if (toFactor != fromFactor && aPar.parameter.field != null) {
			ReflectionUtil.addField(agent.params, aPar.parameter.field, toFactor - fromFactor);
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

}
