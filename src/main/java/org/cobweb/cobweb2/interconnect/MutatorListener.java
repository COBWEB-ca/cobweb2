package org.cobweb.cobweb2.interconnect;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cobweb.cobweb2.core.AgentListener;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.LocationDirection;


public class MutatorListener implements AgentListener {

	private Set<ContactMutator> contactMutators = new LinkedHashSet<ContactMutator>();
	private Set<StepMutator> stepMutators = new LinkedHashSet<StepMutator>();
	private Set<SpawnMutator> spawnMutators = new LinkedHashSet<SpawnMutator>();

	public void addMutator(AgentMutator mutator) {
		if (mutator instanceof SpawnMutator)
			spawnMutators.add((SpawnMutator) mutator);

		if (mutator instanceof ContactMutator)
			contactMutators.add((ContactMutator) mutator);

		if (mutator instanceof StepMutator)
			stepMutators.add((StepMutator) mutator);
	}


	public void removeMutator(AgentMutator mutator) {
		if (mutator instanceof SpawnMutator)
			spawnMutators.remove(mutator);

		if (mutator instanceof ContactMutator)
			contactMutators.remove(mutator);

		if (mutator instanceof StepMutator)
			stepMutators.remove(mutator);
	}

	public void clearMutators() {
		spawnMutators.clear();
		contactMutators.clear();
		stepMutators.clear();
	}

	@Override
	public void onContact(ComplexAgent bumper, ComplexAgent bumpee) {
		for (ContactMutator mut : contactMutators) {
			mut.onContact(bumper, bumpee);
		}
	}

	@Override
	public void onStep(ComplexAgent agent, LocationDirection from, LocationDirection to) {
		for (StepMutator m : stepMutators) {
			m.onStep(agent, from, to);
		}
	}

	@Override
	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2) {
		for (SpawnMutator mutator : spawnMutators) {
			mutator.onSpawn(agent, parent1, parent2);
		}
	}

	@Override
	public void onSpawn(ComplexAgent agent, ComplexAgent parent) {
		for (SpawnMutator mutator : spawnMutators) {
			mutator.onSpawn(agent, parent);
		}
	}

	@Override
	public void onSpawn(ComplexAgent agent) {
		for (SpawnMutator mutator : spawnMutators) {
			mutator.onSpawn(agent);
		}
	}

	@Override
	public void onDeath(ComplexAgent agent) {
		for (SpawnMutator mutator : spawnMutators) {
			mutator.onDeath(agent);
		}
	}



	public Collection<String> logDataAgent(int i) {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logDataAgent(i))
				blah.add(s);
		}
		return blah;
	}

	public Iterable<String> logDataTotal() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logDataTotal())
				blah.add(s);
		}
		return blah;
	}

	public Collection<String> logHederAgent() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logHeadersAgent())
				blah.add(s);
		}
		return blah;
	}

	public Iterable<String> logHederTotal() {
		List<String> blah = new LinkedList<String>();
		for (SpawnMutator mut : spawnMutators) {
			for (String s : mut.logHeaderTotal())
				blah.add(s);
		}
		return blah;
	}



}
