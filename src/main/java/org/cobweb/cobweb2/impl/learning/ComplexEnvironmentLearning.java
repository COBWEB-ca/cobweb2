package org.cobweb.cobweb2.impl.learning;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.impl.ComplexEnvironment;


public class ComplexEnvironmentLearning extends ComplexEnvironment {

	public ComplexEnvironmentLearning(SimulationInternals simulation, LearningParams lparams) { // NO_UCD (unused code) called through reflection
		super(simulation);
		this.learningData = lparams.agentParams;
	}

	private LearningAgentParams learningData[];

	public List<Occurrence> allOccurrences = new LinkedList<Occurrence>();

	@Override
	protected Agent spawnAgent(LocationDirection location, int agentType) {
		ComplexAgentLearning child = (ComplexAgentLearning)super.spawnAgent(location, agentType);
		child.lParams = learningData[agentType].clone();
		return child;
	}

	@Override
	public synchronized void update() {
		super.update();

		pruneOccurrences();
	}

	private void pruneOccurrences() {
		Iterator<Occurrence> iterator = allOccurrences.iterator();
		while (iterator.hasNext()) {
			Occurrence oc = iterator.next();
			if (oc.time < simulation.getTime()) {
				// remove old Occurrence
				iterator.remove();
			}
		}
	}
}
