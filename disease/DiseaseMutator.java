/**
 *
 */
package disease;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cobweb.globals;
import cobweb.params.ReflectionUtil;
import cwcore.ComplexAgent;
import cwcore.complexParams.ContactMutator;
import cwcore.complexParams.SpawnMutator;
import disease.DiseaseParams.DiseaseEffect;

/**
 *
 */
public class DiseaseMutator implements ContactMutator, SpawnMutator {


	private DiseaseParams[] params;

	public DiseaseMutator() {

	}

	public void bump(ComplexAgent bumper, ComplexAgent bumpee) {
		transmitBumpOneWay(bumper, bumpee);
		transmitBumpOneWay(bumpee, bumper);
	}

	private void transmitBumpOneWay(ComplexAgent bumper, ComplexAgent bumpee) {
		int tr = bumper.type();
		int te = bumpee.type();
		if (sick.get(bumper) && params[tr].transmitTo[te] && !sick.get(bumpee)) {
			makeRandomSick(bumpee, params[te].contactTransmitRate);
		}
	}

	public Collection<String> logDataAgent(int agentType) {
		return blank;
	}

	private static final Collection<String> blank = new LinkedList<String>();

	public Collection<String> logDataTotal() {
		return blank;
	}

	public Collection<String> logHeaderTotal() {
		return blank;
	}

	public Collection<String> logHeadersAgent() {
		return blank;
	}

	public void onDeath(ComplexAgent agent) {
		sick.remove(agent);
	}

	public void onSpawn(ComplexAgent agent) {
		makeRandomSick(agent, params[agent.type()].initialInfection);
	}

	private void makeRandomSick(ComplexAgent agent, float rate) {
		boolean isSick = false;
		if (globals.random.nextFloat() < rate)
			isSick = true;

		if (isSick) {
			DiseaseEffect effect = params[agent.type()].effect;
			Field f = effect.param.field;
			if (f != null)
				ReflectionUtil.multiplyField(agent.params, f, effect.factor);
			Color org = agent.getColor();
			Color n = new Color(org.getRed(), org.getGreen(), 255);
			agent.setColor(n);
		}
		sick.put(agent, isSick);
	}

	Map<ComplexAgent, Boolean> sick = new HashMap<ComplexAgent, Boolean>();

	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		if (parent.isAlive() && sick.get(parent))
			makeRandomSick(agent, params[agent.type()].childTransmitRate);
		else
			sick.put(agent, false);
	}

	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2) {
		if ((parent1.isAlive() && sick.get(parent1)) || (parent2.isAlive() && sick.get(parent2)))
			makeRandomSick(agent, params[agent.type()].childTransmitRate);
		else
			sick.put(agent, false);
	}

	public void setParams(DiseaseParams[] diseaseParams, int agentTypes) {
		this.params = diseaseParams;
	}

}
