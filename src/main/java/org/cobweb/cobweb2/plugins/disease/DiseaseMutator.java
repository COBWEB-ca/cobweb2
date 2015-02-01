/**
 *
 */
package org.cobweb.cobweb2.plugins.disease;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.cobweb2.plugins.ContactMutator;
import org.cobweb.cobweb2.plugins.LoggingMutator;
import org.cobweb.cobweb2.plugins.SpawnMutator;
import org.cobweb.cobweb2.plugins.StatefulMutatorBase;
import org.cobweb.cobweb2.plugins.UpdateMutator;
import org.cobweb.util.ArrayUtilities;

/**
 * Simulates various diseases that can affect agents.
 */
public class DiseaseMutator extends StatefulMutatorBase<DiseaseState> implements ContactMutator, SpawnMutator, LoggingMutator, UpdateMutator {

	private DiseaseParams params;

	private int sickCount[] = new int[0];

	private SimulationTimeSpace simulation;

	public DiseaseMutator() {
		super(DiseaseState.class);
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
			DiseaseAgentParams agentParams = params.agentParams[agent.getType()];
			agentParams.param.modifyValue(agent, agentParams.factor, 0);

			sickCount[agent.getType()]++;

			setAgentState(agent, new DiseaseState(agentParams, true, false, simulation.getTime()));
		}

	}

	@Override
	public void onContact(Agent bumper, Agent bumpee) {
		transmitBumpOneWay(bumper, bumpee);
		transmitBumpOneWay(bumpee, bumper);
	}

	@Override
	public void onDeath(Agent agent) {
		DiseaseState diseaseState = removeAgentState(agent);
		if (diseaseState != null && diseaseState.sick)
			sickCount[agent.getType()]--;
	}

	@Override
	public void onSpawn(Agent agent) {
		makeRandomSick(agent, params.agentParams[agent.getType()].initialInfection);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent) {
		if (parent.isAlive() && isSick(parent))
			makeRandomSick(agent, params.agentParams[agent.getType()].childTransmitRate);
		else
			makeRandomSick(agent, 0);
	}

	@Override
	public void onSpawn(Agent agent, Agent parent1, Agent parent2) {
		if ((parent1.isAlive() && isSick(parent1)) || (parent2.isAlive() && isSick(parent2)))
			makeRandomSick(agent, params.agentParams[agent.getType()].childTransmitRate);
		else
			makeRandomSick(agent, 0);
	}

	public void setParams(SimulationTimeSpace sim, DiseaseParams diseaseParams, int agentTypes) {
		this.simulation = sim;
		this.params = diseaseParams;
		sickCount = ArrayUtilities.resizeArray(sickCount, agentTypes);
	}

	private void transmitBumpOneWay(Agent bumper, Agent bumpee) {
		int tr = bumper.getType();
		int te = bumpee.getType();

		if (params.agentParams[tr].vaccinator && !isSick(bumpee) ) {
			vaccinate(bumpee, params.agentParams[tr].vaccineEffectiveness);
		}

		if (params.agentParams[tr].healer && isSick(bumpee)) {
			if (simulation.getRandom().nextFloat() < params.agentParams[tr].healerEffectiveness) {
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

		if (params.agentParams[tr].transmitTo[te]) {
			makeRandomSick(bumpee, params.agentParams[te].contactTransmitRate);
		}
	}

	private void unSick(Agent agent) {
		removeAgentState(agent);
		sickCount[agent.getType()]--;
	}

	public boolean isSick(Agent agent) {
		return hasAgentState(agent) && getAgentState(agent).sick;
	}

	private boolean isVaccinated(Agent agent) {
		return hasAgentState(agent) && getAgentState(agent).vaccinated;
	}

	private void vaccinate(Agent bumpee, float effectiveness) {
		DiseaseAgentParams agentParams = params.agentParams[bumpee.getType()];
		setAgentState(bumpee, new DiseaseState(agentParams, false, true, effectiveness));
	}

	@Override
	public void onUpdate(Agent a) {
		DiseaseState s = getAgentState(a);

		if (params.agentParams[a.getType()].recoveryTime == 0)
			return;

		long randomRecovery = (long) (params.agentParams[a.getType()].recoveryTime * (simulation.getRandom().nextDouble() * 0.2 + 1.0));

		if (s.sick && simulation.getTime() - s.sickStart > randomRecovery) {
			unSick(a);
		}
	}

}
