package org.cobweb.cobweb2.eventlearning;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.SimulationInternals;


public class ComplexEnvironmentLearning extends ComplexEnvironment {

	public ComplexEnvironmentLearning(SimulationInternals simulation) {
		super(simulation);
	}

	private LearningAgentParams learningData[];

	public List<Occurrence> allOccurrences = new LinkedList<Occurrence>();

	@Override
	protected void copyParamsFromParser(SimulationConfig p) {
		super.copyParamsFromParser(p);
		learningData = p.getLearningParams().getLearningAgentParams();
	}

	@Override
	protected void spawnAgent(LocationDirection location, int agentType) {
		ComplexAgentLearning child = (ComplexAgentLearning)simulation.newAgent();
		child.init(this, location, agentData[agentType],
				learningData[agentType]);
	}

	@Override
	public synchronized void update(long tick) {
		super.update(tick);

		pruneOccurrences();
	}

	protected void pruneOccurrences() {
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
