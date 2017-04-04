package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;

public abstract class CyclicFactor extends AbioticFactor {

	@ConfXMLTag("cyclePeriod")
	@ConfDisplayName("Cycle period")
	public int cyclePeriod = 200;

	@ConfXMLTag("cycleOffset")
	@ConfDisplayName("Cycle offset")
	public int cycleOffset = 0;

	@ConfXMLTag("cycleMode")
	@ConfDisplayName("Cycle mode")
	public CycleMode cycleMode = CycleMode.PingPong;

	private float timeExponent = 1;

	@ConfXMLTag("timeExponent")
	@ConfDisplayName("Time exponent")
	public void setExponent(float exponent) {
		this.timeExponent = exponent;
		midpointScale =  1 / (float) (Math.pow(.5, exponent) * 2);
	}

	public float getExponent() {
		return timeExponent;
	}

	private float midpointScale = 1;

	protected float time = 0;

	@Override
	public void update(SimulationTimeSpace sim) {
		super.update(sim);
		time = ((sim.getTime() + cycleOffset) % cyclePeriod) / (float) cyclePeriod;
		while (time < 0)
			time += 1;

		if (cycleMode == CycleMode.PingPong) {
			if (time > .5)
				time = 1 - time;
			time *= 2;
		}

		time = (float)(
				time < 0.5
				? Math.pow(time, timeExponent) * midpointScale
						: 1 - Math.pow(1 - time, timeExponent) * midpointScale);
	}

	public enum CycleMode {
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

	private static final long serialVersionUID = 1L;
}
