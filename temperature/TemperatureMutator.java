package temperature;

import java.util.Collection;
import java.util.LinkedList;

import cobweb.Environment;
import cobweb.Environment.Location;
import cobweb.params.ReflectionUtil;
import cwcore.ComplexAgent;
import cwcore.complexParams.AgentFoodCountable;
import cwcore.complexParams.SpawnMutator;
import cwcore.complexParams.StepMutator;

public class TemperatureMutator implements StepMutator, SpawnMutator {

	private TemperatureParams params;

	public TemperatureMutator() {
		
	}
	
	public void setParams(TemperatureParams params, AgentFoodCountable env) {
		this.params = params;
		
	}
	
	private static final Collection<String> blank = new LinkedList<String>();

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

	}

	public void onSpawn(ComplexAgent agent) {
		TemperatureAgentParams aPar = params.agentParams[agent.type()];
		
		float f = locToPenalty(agent.getPosition(), aPar);
		if (aPar.parameter.field != null)
			ReflectionUtil.addField(agent.params, aPar.parameter.field, f);
	}

	private float locToPenalty(Location l, TemperatureAgentParams aPar) {
		int temp = getTemp(l);
		int ptemp = aPar.preferedTemp;
		int diff = Math.abs(temp - ptemp);
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
	
	private int getTemp(Location loc) {
		int lat = loc.v[1] * 5 / loc.getEnvironment().getSize(Environment.AXIS_Y);
		return params.tempBands[lat];
	}

}
