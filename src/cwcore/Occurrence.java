package cwcore;

import cwcore.MemorableEvent.BreedMemEvent;
import cwcore.MemorableEvent.EnergyMemEvent;

/*
 * Occurrences represent tangible events that occur in an environment
 */
public abstract class Occurrence {

	float detectableDistance;
	ComplexAgent target;
	long time;
	MemorableEvent event;
	private boolean hasOccurred = false;

	public Occurrence(ComplexAgent target, float detectableDistance) {
		this.target = target;
		time = target.getCurrTick();
		this.detectableDistance = detectableDistance;
	}

	// Effects the agent in whatever way necessary returning a memory of the
	// effect
	abstract MemorableEvent effect(ComplexAgent concernedAgent);

	// Causes the effect to occur,  initializes the event, places the memory in the agent's memory,
	// returns the event
	final MemorableEvent occur() {
		event = effect(target);

		//target.remember(event);
		hasOccurred = true;

		return event;
	}

	public MemorableEvent getEvent() {
		if (event == null) {
			throw new NullPointerException("Must call occur() before calling getEvent()!");
		}
		return event;
	}

	public boolean hasOccurred() {
		return hasOccurred;
	}

	//SUBCLASSES:
	public static class EnergyChangeOccurrence extends Occurrence {

		private int amountChanged;

		public EnergyChangeOccurrence(ComplexAgent target, int amountChanged) {
			this(target, amountChanged, 0);
		}

		public EnergyChangeOccurrence(ComplexAgent target, float detectableDistance, int amountChanged) {
			super(target, detectableDistance);
			this.amountChanged = amountChanged;
		}

		@Override
		public MemorableEvent effect(ComplexAgent concernedAgent) {
			int originalEnergy = concernedAgent.getEnergy();
			concernedAgent.changeEnergy(amountChanged);
			float magnitude = (float) originalEnergy / (float) concernedAgent.getEnergy();
			return new EnergyMemEvent(time, magnitude, this);
		}

		public int getAmountChanged() {
			return amountChanged;
		}
	}

	public static class BreedOccurrence extends Occurrence {

		private long partnerID;

		public BreedOccurrence(ComplexAgent target, float detectableDistance, long partnerID) {
			super(target, detectableDistance);
			this.partnerID = partnerID;
		}		

		public long getPartnerID() {
			return partnerID;
		}

		@Override
		public MemorableEvent effect(ComplexAgent concernedAgent) {
			System.out.println("BREEDING!");

			// Setting breedPos to non-null will cause the agent
			// to breed later
			concernedAgent.setBreedPos(concernedAgent.getPosition());

			// Subtract starting energy and energy penalty from
			// energy
			int energyLost = (int) (concernedAgent.params.initEnergy + concernedAgent.energyPenalty(true));

			EnergyChangeOccurrence energyChange = new EnergyChangeOccurrence(concernedAgent, 5f, 
					-energyLost);
			energyChange.occur();

			concernedAgent.setWasteCounterLoss(concernedAgent.getWasteCounterLoss() - concernedAgent.params.initEnergy);

			concernedAgent.getInfo().useOthers(concernedAgent.params.initEnergy);			

			return new BreedMemEvent(time, +0.5f, this);
		}

	}

}
