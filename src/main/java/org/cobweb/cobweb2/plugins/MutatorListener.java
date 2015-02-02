package org.cobweb.cobweb2.plugins;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.AgentListener;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.LocationDirection;


public class MutatorListener implements AgentListener {

	private Set<SpawnMutator> spawnMutators = new LinkedHashSet<>();
	private Set<ContactMutator> contactMutators = new LinkedHashSet<>();
	private Set<StepMutator> stepMutators = new LinkedHashSet<>();
	private Set<EnergyMutator> energyMutators = new LinkedHashSet<>();
	private Set<UpdateMutator> updateMutators = new LinkedHashSet<>();
	private Set<LoggingMutator> loggingMutators = new LinkedHashSet<>();

	public void addMutator(AgentMutator mutator) {
		if (mutator instanceof SpawnMutator)
			spawnMutators.add((SpawnMutator) mutator);

		if (mutator instanceof ContactMutator)
			contactMutators.add((ContactMutator) mutator);

		if (mutator instanceof StepMutator)
			stepMutators.add((StepMutator) mutator);

		if (mutator instanceof EnergyMutator)
			energyMutators.add((EnergyMutator) mutator);

		if (mutator instanceof UpdateMutator)
			updateMutators.add((UpdateMutator) mutator);

		if (mutator instanceof LoggingMutator)
			loggingMutators.add((LoggingMutator) mutator);

	}


	public void removeMutator(AgentMutator mutator) {
		spawnMutators.remove(mutator);
		contactMutators.remove(mutator);
		stepMutators.remove(mutator);
		energyMutators.remove(mutator);
		updateMutators.remove(mutator);
		loggingMutators.remove(mutator);
	}

	public void clearMutators() {
		spawnMutators.clear();
		contactMutators.clear();
		stepMutators.clear();
		energyMutators.clear();
		updateMutators.clear();
		loggingMutators.clear();
	}

	@Override
	public void onContact(Agent bumper, Agent bumpee) {
		for (ContactMutator mut : contactMutators) {
			mut.onContact(bumper, bumpee);
		}
	}

	@Override
	public void onStep(Agent agent, LocationDirection from, LocationDirection to) {
		for (StepMutator m : stepMutators) {
			m.onStep(agent, from, to);
		}
	}

	@Override
	public void onSpawn(Agent agent, Agent parent1, Agent parent2) {
		for (SpawnMutator mutator : spawnMutators) {
			mutator.onSpawn(agent, parent1, parent2);
		}
	}

	@Override
	public void onSpawn(Agent agent, Agent parent) {
		for (SpawnMutator mutator : spawnMutators) {
			mutator.onSpawn(agent, parent);
		}
	}

	@Override
	public void onSpawn(Agent agent) {
		for (SpawnMutator mutator : spawnMutators) {
			mutator.onSpawn(agent);
		}
	}

	@Override
	public void onDeath(Agent agent) {
		for (SpawnMutator mutator : spawnMutators) {
			mutator.onDeath(agent);
		}
	}

	@Override
	public void onEnergyChange(Agent agent, int delta, Cause cause) {
		for(EnergyMutator mutator : energyMutators) {
			mutator.onEnergyChange(agent, delta, cause);
		}
	}

	@Override
	public void onUpdate(Agent agent) {
		for(UpdateMutator mutator : updateMutators) {
			mutator.onUpdate(agent);
		}
	}

	public List<String> logDataAgent(int type) {
		List<String> res = new LinkedList<String>();
		for (LoggingMutator mut : loggingMutators) {
			res.addAll(mut.logDataAgent(type));
		}
		return res;
	}

	public List<String> logDataTotal() {
		List<String> res = new LinkedList<String>();
		for (LoggingMutator mut : loggingMutators) {
			res.addAll(mut.logDataTotal());
		}
		return res;
	}

	public List<String> logHeaderAgent() {
		List<String> res = new LinkedList<String>();
		for (LoggingMutator mut : loggingMutators) {
			res.addAll(mut.logHeadersAgent());
		}
		return res;
	}

	public List<String> logHeaderTotal() {
		List<String> res = new LinkedList<String>();
		for (LoggingMutator mut : loggingMutators) {
			res.addAll(mut.logHeaderTotal());
		}
		return res;
	}

}
