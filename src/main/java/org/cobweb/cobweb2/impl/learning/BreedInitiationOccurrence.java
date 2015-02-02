package org.cobweb.cobweb2.impl.learning;

import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexAgent.ReproductionCause;

public class BreedInitiationOccurrence extends Occurrence {

	private ComplexAgent partner;
	private ReproductionCause cause;

	public BreedInitiationOccurrence(ComplexAgentLearning target, long time, float detectableDistance, ReproductionCause cause, ComplexAgent partner) {
		super(target, time, detectableDistance, cause.getName());
		this.cause = cause;
		this.partner = partner;
	}

	public ComplexAgent getPartnerID() {
		return partner;
	}

	@Override
	public MemorableEvent effect(ComplexAgentLearning concernedAgent) {
		// Setting breedPos to non-null will cause the agent
		// to breed later
		concernedAgent.setBreedPos(concernedAgent.getPosition());

		// Subtract starting energy and energy penalty from
		// energy
		int energyLost = concernedAgent.params.initEnergy + concernedAgent.energyPenalty();

		EnergyChangeOccurrence energyChange = new EnergyChangeOccurrence(concernedAgent, time, 5f, cause, -energyLost);
		energyChange.happen();

		return new MemorableEvent(time, +0.5f, "breedInit");
	}

}