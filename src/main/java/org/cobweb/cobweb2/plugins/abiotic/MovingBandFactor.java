package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.MathUtil;


public class MovingBandFactor extends AbioticFactor {


	@ConfXMLTag("bgValue")
	@ConfDisplayName("Background value")
	public float bgValue = 0f;

	@ConfXMLTag("bandValue")
	@ConfDisplayName("Band value")
	public float bandValue = 1f;

	@ConfXMLTag("bandWidth")
	@ConfDisplayName("Band width")
	public float bandWidth = 0.15f;

	@ConfXMLTag("transition")
	@ConfDisplayName("Edge hardness")
	public float transition = 4f;

	@ConfXMLTag("posStart")
	@ConfDisplayName("Start position")
	public float posStart = 0.2f;

	@ConfXMLTag("posEnd")
	@ConfDisplayName("End position")
	public float posEnd = 1f;

	@ConfXMLTag("wrap")
	@ConfDisplayName("Wrap")
	public boolean wrap = false;

	@ConfXMLTag("angle")
	@ConfDisplayName("Angle")
	public void setAngle(float value) {
		while (value > 180)
			value -= 360;
		while (value <= -180)
			value += 360;
		angle = value;
	}
	public float getAngle() {
		return angle;
	}
	private float angle = 90f;


	@ConfXMLTag("cyclePeriod")
	@ConfDisplayName("Cycle period")
	public int cyclePeriod = 200;

	@ConfXMLTag("cycleMode")
	@ConfDisplayName("Cycle mode")
	public CycleMode cycleMode = CycleMode.PingPong;

	private float time;

	public static enum CycleMode {
		Loop("Loop"),
		PingPong("Ping-Pong");

		private final String value;

		private CycleMode(String s) {
			value = s;
		}

		/**
		 * Gets CycleMode from friendly name
		 * @param s friendly name
		 * @return MeiosisMode with given name
		 * @throws IllegalArgumentException when no CycleMode has given friendly name
		 */
		public static CycleMode fromString(String s) { // NO_UCD called through reflection
			for (CycleMode m : CycleMode.values()) {
				if (m.value.equals(s))
					return m;
			}
			throw new IllegalArgumentException("Invalid value");
		}

		@Override
		public String toString() {
			return value;
		}
	}

	@Override
	public void update(SimulationTimeSpace sim) {
		super.update(sim);
		time = (sim.getTime() % cyclePeriod) / (float) cyclePeriod;

		if (cycleMode == CycleMode.PingPong) {
			if (time > .5)
				time = 1 - time;
			time *= 2;
		}
	}

	@Override
	public float getValue(float x, float y) {
		float z = (float) MathUtil.pointLineDistInSquare(x - .5, y - .5, Math.toRadians(-angle)) + .5f;
		float p = time * (posEnd - posStart) + posStart;

		float distance = Math.abs(p - z);

		if (wrap && distance > 0.5f)
			distance = 1f - distance;

		distance -= bandWidth;

		float t =  1.0f / (float) (1 + Math.exp(-distance * 10 * transition));
		t = MathUtil.clamp(t, 0, 1);

		return bgValue * t + bandValue * (1 - t);
	}

	@Override
	public float getMax() {
		return Math.max(bgValue, bandValue);
	}

	@Override
	public float getMin() {
		return Math.min(bgValue, bandValue);
	}

	@Override
	public String getName() {
		return "Moving Band";
	}

	@Override
	public AbioticFactor copy() {
		try {
			MovingBandFactor copy = (MovingBandFactor) super.clone();
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

}
