/**
 *
 */
package org.cobweb.cobweb2.plugins.disease;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.Updatable;
import org.cobweb.cobweb2.plugins.StatefulMutatorBase;
import org.cobweb.cobweb2.plugins.ContactMutator;
import org.cobweb.cobweb2.plugins.LoggingMutator;
import org.cobweb.cobweb2.plugins.SpawnMutator;
import org.cobweb.util.ArrayUtilities;

/**
 * Simulates various diseases that can affect agents.
 */
public class DiseaseMutator extends StatefulMutatorBase<DiseaseMutator.State> implements ContactMutator, SpawnMutator, LoggingMutator, Updatable {

	private DiseaseParams[] params;

	private int sickCount[] = new int[0];

	private SimulationInternals simulation;

	public DiseaseMutator() {
		super(DiseaseMutator.State.class);
	}

	public class State {
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

	private void makeRandomSick(Agent agent, float rate) {
		boolean isSick = false;
		if (simulation.getRandom().nextFloat() < rate)
			isSick = true;

		if (isSick) {
			DiseaseParams effect = params[agent.getType()];
			effect.param.modifyValue(agent, effect.factor, 0);

			sickCount[agent.getType()]++;

			setAgentState(agent, new State(true, false, simulation.getTime()));
		}

	}

	@Override
	public void onContact(Agent bumper, Agent bumpee) {
		transmitBumpOneWay(bumper, bumpee);
		transmitBumpOneWay(bumpee, bumper);
	}

	@Override
	public void onDeath(Agent agent) {
		State state = removeAgentState(agent);
		if (state != null && state.sick)
			sickCount[agent.getType()]--;
	}

	@Override
	public void onSpawn(Agent agent) {
		makeRandomSick(agent, params[agent.getType()].initialInfection);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent) {
		if (parent.isAlive() && isSick(parent))
			makeRandomSick(agent, params[agent.getType()].childTransmitRate);
		else
			makeRandomSick(agent, 0);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent1, Agent parent2) {
		if ((parent1.isAlive() && isSick(parent1)) || (parent2.isAlive() && isSick(parent2)))
			makeRandomSick(agent, params[agent.getType()].childTransmitRate);
		else
			makeRandomSick(agent, 0);
	}

	public void setParams(SimulationInternals sim, DiseaseParams[] diseaseParams, int agentTypes) {
		this.simulation = sim;
		this.params = diseaseParams;
		sickCount = ArrayUtilities.resizeArray(sickCount, agentTypes);
	}

	private void transmitBumpOneWay(Agent bumper, Agent bumpee) {
		int tr = bumper.getType();
		int te = bumpee.getType();

		if (params[tr].vaccinator && !isSick(bumpee) ) {
			vaccinate(bumpee, params[tr].vaccineEffectiveness);
		}

		if (params[tr].healer && isSick(bumpee)) {
			if (simulation.getRandom().nextFloat() < params[tr].healerEffectiveness) {
				unSick(bumpee);
			}
		}

		if (!isSick(bumper))
			return;

		if (isVaccinated(bumpee)
				&& simulation.getRandom().nextFloat() < getAgentState(bumpee).vaccineEffectiveness)
			return;

		if (isSick(bumpee))
			return;

		if (params[tr].transmitTo[te]) {
			makeRandomSick(bumpee, params[te].contactTransmitRate);
		}
	}

	private void unSick(Agent agent) {
		removeAgentState(agent);
		sickCount[agent.getType()]--;
	}

	private void unSickIterating(Agent agent, Iterator<Agent> agents) {
		agents.remove();
		sickCount[agent.getType()]--;
	}

	public boolean isSick(Agent agent) {
		return hasAgentState(agent) && getAgentState(agent).sick;
	}

	private boolean isVaccinated(Agent agent) {
		return hasAgentState(agent) && getAgentState(agent).vaccinated;
	}

	private void vaccinate(Agent bumpee, float effectiveness) {
		setAgentState(bumpee, new State(false, true, effectiveness));
	}

	@Override
	public void update() {

		for (Iterator<Agent> agents = getAgentsWithState().iterator(); agents.hasNext();) {
			Agent a = agents.next();
			State s = getAgentState(a);

			if (params[a.getType()].recoveryTime == 0)
				continue;

			long randomRecovery = (long) (params[a.getType()].recoveryTime * (simulation.getRandom().nextDouble() * 0.2 + 1.0));

			if (s.sick && simulation.getTime() - s.sickStart > randomRecovery) {
				unSickIterating(a, agents);
			}
		}
	}

}
