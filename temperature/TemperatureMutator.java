package temperature;

import java.util.Collection;
import java.util.LinkedList;

import cobweb.Environment.Location;
import cobweb.params.ReflectionUtil;
import cwcore.ComplexAgent;
import cwcore.complexParams.AgentFoodCountable;
import cwcore.complexParams.SpawnMutator;
import cwcore.complexParams.StepMutator;

public class TemperatureMutator implements StepMutator, SpawnMutator {

	private TemperatureParams params;

	public TemperatureMutator() {
		// Nothing
	}

	public void setParams(TemperatureParams params, AgentFoodCountable env) {
		this.params = params;
		height = env.getHeight();
		bandNumber = Math.min(TemperatureParams.TEMPERATURE_BANDS, height);
	}

	private static final Collection<String> blank = new LinkedList<String>();
	private int bandNumber;
	private int height;

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

	private float locToPenalty(Location l, TemperatureAgentParams aPar) {
		float temp = getTemp(l);
		float ptemp = aPar.preferedTemp;
		float diff = Math.abs(temp - ptemp);
		diff = Math.max(diff - aPar.preferedTempRange, 0);
		float f = diff * aPar.differenceFactor;
		return f;
	}

	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		onSpawn(agent);
	}

	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2) {
		onSpawn(agent);
	}

	public void onStep(ComplexAgent agent, Location to, Location from) {
		TemperatureAgentParams aPar = params.agentParams[agent.type()];

		float toFactor = locToPenalty(to, aPar);
		float fromFactor = locToPenalty(from, aPar);

		if (toFactor != fromFactor && aPar.parameter.field != null) {
			ReflectionUtil.addField(agent.params, aPar.parameter.field, toFactor - fromFactor);
		}
	}

	private float getTemp(Location loc) {
		int lat = loc.v[1] * bandNumber / height;
		return params.tempBands[lat];
	}

}
