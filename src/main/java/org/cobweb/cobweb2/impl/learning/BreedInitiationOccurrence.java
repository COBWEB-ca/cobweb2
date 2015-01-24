package org.cobweb.cobweb2.impl.learning;

import org.cobweb.cobweb2.impl.ComplexAgent;

public class BreedInitiationOccurrence extends Occurrence {

	private ComplexAgent partner;

	public BreedInitiationOccurrence(ComplexAgentLearning target, long time, float detectableDistance, String desc, ComplexAgent partner) {
		super(target, time, detectableDistance, desc);
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

		EnergyChangeOccurrence energyChange = new EnergyChangeOccurrence(concernedAgent, time, 5f, "breed", -energyLost);
		energyChange.happen();

		concernedAgent.setWasteCounterLoss(concernedAgent.getWasteCounterLoss() - concernedAgent.params.initEnergy);

		concernedAgent.getInfo().useReproductionEnergy(concernedAgent.params.initEnergy);

		return new MemorableEvent(time, +0.5f, "breedInit");
	}

}