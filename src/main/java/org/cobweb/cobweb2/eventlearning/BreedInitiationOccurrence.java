package org.cobweb.cobweb2.eventlearning;

import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.ComplexAgentLearning;

public class BreedInitiationOccurrence extends Occurrence {

	private ComplexAgent partner;

	public BreedInitiationOccurrence(ComplexAgentLearning target, float detectableDistance, String desc, ComplexAgent partner) {
		super(target, detectableDistance, desc);
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
		int energyLost = (int) (concernedAgent.params.initEnergy + concernedAgent.energyPenalty());

		EnergyChangeOccurrence energyChange = new EnergyChangeOccurrence(concernedAgent, 5f, "breed", -energyLost);
		energyChange.happen();

		concernedAgent.setWasteCounterLoss(concernedAgent.getWasteCounterLoss() - concernedAgent.params.initEnergy);

		concernedAgent.getInfo().useOthers(concernedAgent.params.initEnergy);

		return new MemorableEvent(time, +0.5f, "breedInit");
	}

}