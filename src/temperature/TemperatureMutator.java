package temperature;

import java.util.Collection;
import java.util.LinkedList;

import cobweb.Environment.Location;
import cobweb.params.ReflectionUtil;
import cwcore.ComplexAgent;
import cwcore.complexParams.AgentFoodCountable;
import cwcore.complexParams.SpawnMutator;
import cwcore.complexParams.StepMutator;

/**
 * TemperatureMutator is an instance of Step and Spawn Mutator
 * 
 * @author ???
 */
public class TemperatureMutator implements StepMutator, SpawnMutator {

	private TemperatureParams params;

	private static final Collection<String> blank = new LinkedList<String>();

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
		int lat = loc.v[1] * bandNumber / height;
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

	public Collection<String> logDataAgent(int agentType) {
		return blank;
	}

	public Collection<String> logDataTotal() {
		return blank;
	}

	public Collection<String> logHeadersAgent() {
		return blank;
	}

	public Collection<String> logHeaderTotal() {
		return blank;
	}

	public void onDeath(ComplexAgent agent) {
		// Nothing
	}

	public void onSpawn(ComplexAgent agent) {
		TemperatureAgentParams aPar = params.agentParams[agent.type()];

		float f = locToPenalty(agent.getPosition(), aPar);
		if (aPar.parameter.field != null)
			ReflectionUtil.addField(agent.params, aPar.parameter.field, f);
	}

	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		onSpawn(agent);
	}

	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2) {
		onSpawn(agent);
	}

	/**
	 * During a step 
	 * 
	 * @param agent The agent doing the step
	 * @param to Location the agent is moving to.
	 * @param from Location the agent is moving from.
	 */
	public void onStep(ComplexAgent agent, Location to, Location from) {
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
