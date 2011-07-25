/**
 *
 */
package disease;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import cobweb.ArrayUtilities;
import cobweb.globals;
import cobweb.params.ReflectionUtil;
import cwcore.ComplexAgent;
import cwcore.complexParams.ContactMutator;
import cwcore.complexParams.SpawnMutator;

/**
 * Simulates various diseases that can affect agents.
 */
public class DiseaseMutator implements ContactMutator, SpawnMutator, cobweb.TickScheduler.Client {

	private DiseaseParams[] params;

	private static final Collection<String> blank = new LinkedList<String>();

	private int sickCount[];

	Map<ComplexAgent, State> sick = new HashMap<ComplexAgent, State>();

	private long time;

	private class State {
		public boolean sick = false;
		public boolean vaccinated = false;
		public long sickStart = -1;

		public State(boolean sick, boolean vaccinated, long sickStart) {
			this.sick = sick;
			this.vaccinated = true;
			this.sickStart = sickStart;
		}

		public State(boolean sick, boolean vaccinated) {
			this.sick = sick;
			this.vaccinated = vaccinated;
		}
	}

	public DiseaseMutator() {
		sickCount = new int[0];
	}

	public Collection<String> logDataAgent(int agentType) {
		List<String> l = new LinkedList<String>();
		l.add(Integer.toString(sickCount[agentType]));
		return l;
	}

	public Collection<String> logDataTotal() {
		List<String> l = new LinkedList<String>();
		int sum = 0;
		for(int x : sickCount)
			sum += x;

		l.add(Integer.toString(sum));
		return l;
	}

	public Collection<String> logHeadersAgent() {
		List<String> header = new LinkedList<String>();
		header.add("Diseased");
		return header;
	}

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

			Color org = agent.getColor();
			Color n = new Color(org.getRed(), org.getGreen(), 255);
			agent.setColor(n);

			sickCount[agent.type()]++;

			Assert.assertFalse(isVaccinated(agent));
			sick.put(agent, new State(true, false, time));
		}

	}

	public void onContact(ComplexAgent bumper, ComplexAgent bumpee) {
		transmitBumpOneWay(bumper, bumpee);
		transmitBumpOneWay(bumpee, bumper);
	}

	public void onDeath(ComplexAgent agent) {
		State state = sick.remove(agent);
		if (state != null && state.sick)
			sickCount[agent.type()]--;
	}

	public void onSpawn(ComplexAgent agent) {
		makeRandomSick(agent, params[agent.type()].initialInfection);
	}

	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		if (parent.isAlive() && isSick(parent))
			makeRandomSick(agent, params[agent.type()].childTransmitRate);
		else
			makeRandomSick(agent, 0);
	}

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
			vaccinate(bumpee);
		}

		if (params[tr].healer && isSick(bumpee)) {
			sick.remove(bumpee);
			unSick(bumpee);
		}

		if (!isSick(bumper))
			return;

		if (isVaccinated(bumpee))
			return;

		if (isSick(bumpee))
			return;

		if (params[tr].transmitTo[te]) {
			makeRandomSick(bumpee, params[te].contactTransmitRate);
		}
	}

	private void unSick(ComplexAgent agent) {
		// remove is done separately because it may require an iterator like in tickNotification
		sickCount[agent.type()]--;

		// unblue agent
		Color org = agent.getColor();
		Color n = new Color(org.getRed(), org.getGreen(), 0);
		agent.setColor(n);
	}

	private boolean isSick(ComplexAgent agent) {
		return sick.containsKey(agent) && sick.get(agent).sick;
	}

	private boolean isVaccinated(ComplexAgent agent) {
		return sick.containsKey(agent) && sick.get(agent).vaccinated;
	}

	private void vaccinate(ComplexAgent bumpee) {
		sick.put(bumpee, new State(false, true));
	}

	@Override
	public void tickNotification(long time) {
		this.time = time;

		for (Iterator<ComplexAgent> agents = sick.keySet().iterator(); agents.hasNext();) {
			ComplexAgent a = agents.next();
			State s = sick.get(a);

			if (params[a.type()].recoveryTime == 0)
				continue;

			long randomRecovery = (long) (params[a.type()].recoveryTime * (globals.random.nextDouble() * 0.2 + 1.0)); 

			if (s.sick && time - s.sickStart > randomRecovery) {
				agents.remove();
				unSick(a);
			}
		}
	}

	@Override
	public void tickZero() {
		// Nothing
	}

}
