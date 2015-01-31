package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.io.ParameterSerializable;


public abstract class AbioticFactor implements ParameterSerializable {

	public abstract float getValue(float x, float y);

	private static final long serialVersionUID = 1L;
}
