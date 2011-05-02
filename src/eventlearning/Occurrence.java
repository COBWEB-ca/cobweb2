package eventlearning;

import cwcore.ComplexAgent;

public abstract class Occurrence implements Queueable {

	public float detectableDistance;
	public ComplexAgent target;
	public long time;
	public MemorableEvent event;
	private boolean hasOccurred = false;
	String desc;

	public Occurrence(ComplexAgent target, float detectableDistance, String desc) {
		this.target = target;
		time = target.getCurrTick();
		this.detectableDistance = detectableDistance;
		this.desc = desc;
		ComplexAgent.allOccurrences.add(this);
	}

	// Effects the agent in whatever way necessary returning a memory of the
	// effect
	public abstract MemorableEvent effect(ComplexAgent concernedAgent);

	// Causes the effect to occur, initializes the event, places the memory
	// in the agent's memory,
	// returns the event
	public final void happen() {
		event = effect(target);
		target.remember(event);
		hasOccurred = true;
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

	public boolean isComplete() {
		return true;
	}

	public final String getDescription() {
		return desc;
	}
}