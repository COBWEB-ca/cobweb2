package eventlearning;


public class MemorableEvent implements Describeable {

	private final long time;
	private final float magnitude;
	String desc;

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

	public String getDescription() {
		return desc;
	}

	public boolean isDesireable() {
		return magnitude > 0;
	}

	public boolean isDespicable() {
		return magnitude < 0;
	}

	public boolean forgetAfterStep() {
		return false;
	}
}