/**
 *
 */
package org.cobweb.cobweb2.disease;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.Updatable;
import org.cobweb.cobweb2.core.globals;
import org.cobweb.cobweb2.interconnect.ContactMutator;
import org.cobweb.cobweb2.interconnect.SpawnMutator;
import org.cobweb.util.ArrayUtilities;
import org.cobweb.util.ReflectionUtil;

/**
 * Simulates various diseases that can affect agents.
 */
public class DiseaseMutator implements ContactMutator, SpawnMutator, Updatable {

	private DiseaseParams[] params;

	private int sickCount[];

	Map<ComplexAgent, State> sick = new HashMap<ComplexAgent, State>();

	private long time;

	private class State {
		public boolean sick = false;
		public boolean vaccinated = false;
		public long sickStart = -1;
		public float vaccineEffectiveness;

		public State(boolean sick, boolean vaccinated, long sickStart) {
			this.sick = sick;
			this.vaccinated = vaccinated;
			this.sickStart = sickStart;
		}

		public State(boolean sick, boolean vaccinated, float vaccineEffectiveness) {
			this.sick = sick;
			this.vaccinated = vaccinated;
			this.vaccineEffectiveness = vaccineEffectiveness;

		}
	}

	/**
	 * DiseaseMutator is an instance of Contact and Spawn Mutators
	 */
	public DiseaseMutator() {
		sickCount = new int[0];
	}

	@Override
	public Collection<String> logDataAgent(int agentType) {
		List<String> l = new LinkedList<String>();
		l.add(Integer.toString(sickCount[agentType]));
		return l;
	}

	@Override
	public Collection<String> logDataTotal() {
		List<String> l = new LinkedList<String>();
		int sum = 0;
		for(int x : sickCount)
			sum += x;

		l.add(Integer.toString(sum));
		return l;
	}

	@Override
	public Collection<String> logHeadersAgent() {
		List<String> header = new LinkedList<String>();
		header.add("Diseased");
		return header;
	}

	@Override
	public Collection<String> logHeaderTotal() {
		List<String> header = new LinkedList<String>();
		header.add("Diseased");
		return header;
	}

	private void makeRandomSick(ComplexAgent agent, float rate) {
		boolean isSick = false;
		if (globals.random.nextFloat() < rate)
			isSick = true;

		if (isSick) {
			DiseaseParams effect = params[agent.type()];
			Field f = effect.param.field;
			if (f != null)
				ReflectionUtil.multiplyField(agent.params, f, effect.factor);

			sickCount[agent.type()]++;

			sick.put(agent, new State(true, false, time));
		}

	}

	@Override
	public void onContact(ComplexAgent bumper, ComplexAgent bumpee) {
		transmitBumpOneWay(bumper, bumpee);
		transmitBumpOneWay(bumpee, bumper);
	}

	@Override
	public void onDeath(ComplexAgent agent) {
		State state = sick.remove(agent);
		if (state != null && state.sick)
			sickCount[agent.type()]--;
	}

	@Override
	public void onSpawn(ComplexAgent agent) {
		makeRandomSick(agent, params[agent.type()].initialInfection);
	}

	@Override
	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		if (parent.isAlive() && isSick(parent))
			makeRandomSick(agent, params[agent.type()].childTransmitRate);
		else
			makeRandomSick(agent, 0);
	}

	@Override
	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2) {
		if ((parent1.isAlive() && isSick(parent1)) || (parent2.isAlive() && isSick(parent2)))
			makeRandomSick(agent, params[agent.type()].childTransmitRate);
		else
			makeRandomSick(agent, 0);
	}

	public void setParams(DiseaseParams[] diseaseParams, int agentTypes) {
		this.params = diseaseParams;
		sickCount = ArrayUtilities.resizeArray(sickCount, agentTypes);
	}

	private void transmitBumpOneWay(ComplexAgent bumper, ComplexAgent bumpee) {
		int tr = bumper.type();
		int te = bumpee.type();

		if (params[tr].vaccinator && !isSick(bumpee) ) {
			vaccinate(bumpee, params[tr].vaccineEffectiveness);
		}

		if (params[tr].healer && isSick(bumpee)) {
			if (globals.random.nextFloat() < params[tr].healerEffectiveness) {
				unSick(bumpee);
			}
		}

		if (!isSick(bumper))
			return;

		if (isVaccinated(bumpee)
				&& globals.random.nextFloat() < sick.get(bumpee).vaccineEffectiveness)
			return;

		if (isSick(bumpee))
			return;

		if (params[tr].transmitTo[te]) {
			makeRandomSick(bumpee, params[te].contactTransmitRate);
		}
	}

	private void unSick(ComplexAgent agent) {
		sick.remove(agent);
		sickCount[agent.type()]--;
	}

	private void unSickIterating(ComplexAgent agent, Iterator<ComplexAgent> agents) {
		agents.remove();
		sickCount[agent.type()]--;
	}

	private boolean isSick(ComplexAgent agent) {
		return sick.containsKey(agent) && sick.get(agent).sick;
	}

	private boolean isVaccinated(ComplexAgent agent) {
		return sick.containsKey(agent) && sick.get(agent).vaccinated;
	}

	private void vaccinate(ComplexAgent bumpee, float effectiveness) {
		sick.put(bumpee, new State(false, true, effectiveness));
	}

	@Override
	public void update(long time) {
		this.time = time;

		for (Iterator<ComplexAgent> agents = sick.keySet().iterator(); agents.hasNext();) {
			ComplexAgent a = agents.next();
			State s = sick.get(a);

			if (params[a.type()].recoveryTime == 0)
				continue;

			long randomRecovery = (long) (params[a.type()].recoveryTime * (globals.random.nextDouble() * 0.2 + 1.0));

			if (s.sick && time - s.sickStart > randomRecovery) {
				unSickIterating(a, agents);
			}
		}
	}

}
