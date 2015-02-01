package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.io.ParameterSerializable;


public abstract class AbioticFactor implements ParameterSerializable {

	public abstract float getValue(float x, float y);

	private static final long serialVersionUID = 1L;

	public abstract String getName();

	public void update(SimulationTimeSpace sim) {
		sim.getTime();
		// TODO seasonal abiotic factors
	}

	public abstract AbioticFactor copy();
}
