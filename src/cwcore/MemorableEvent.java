package cwcore;

import cwcore.Occurrence.BreedOccurrence;
import cwcore.Occurrence.EnergyChangeOccurrence;

/**
 * A class to store information about an event that the agent ought to learn a
 * lesson from. The event is based on a flag type (so the event may regard an
 * agent, a food, a stone). The magnitude of the event is arbitrary, where -1
 * represents an event that ought to be always avoided, and +1 an event that
 * ought to always be sought after
 */
public class MemorableEvent {

	private final long time;
	private final float magnitude;
	String desc;

	public MemorableEvent(long time, float magnitude) {
		this(time, magnitude, null);
	}

	public MemorableEvent(long time, float magnitude, String desc) {
		if (desc == null) {
			desc = getClass().getName();
		}
		this.time = time;
		this.magnitude = magnitude;
		this.desc = desc;
	}

	public long getTime() {
		return time;
	}

	public float getMagnitude() {
		return magnitude;
	}

	public String getDescriptor() {
		return desc;
	}

	public boolean isDesireable() {
		return magnitude > 0;
	}

	public boolean isDespicable() {
		return magnitude < 0;
	}

	public static class EnergyMemEvent extends MemorableEvent {
		EnergyChangeOccurrence occurrence;
		public EnergyMemEvent(long time, float magnitude, EnergyChangeOccurrence occurrence) {
			super (time, magnitude);
			this.occurrence = occurrence;
		}

		public int getEnergyChange() {
			return occurrence.getAmountChanged();
		}

	}

	public static class BreedMemEvent extends MemorableEvent {
		BreedOccurrence occurrence;
		public BreedMemEvent(long time, float magnitude, BreedOccurrence occurrence) {
			super(time, magnitude);
			this.occurrence = occurrence;
		}

	}

}
