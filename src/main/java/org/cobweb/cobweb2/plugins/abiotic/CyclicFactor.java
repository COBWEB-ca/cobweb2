package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;

public abstract class CyclicFactor extends AbioticFactor {

	@ConfXMLTag("cyclePeriod")
	@ConfDisplayName("Cycle period")
	public int cyclePeriod = 200;

	@ConfXMLTag("cycleMode")
	@ConfDisplayName("Cycle mode")
	public CycleMode cycleMode = CycleMode.PingPong;

	protected float time;

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
