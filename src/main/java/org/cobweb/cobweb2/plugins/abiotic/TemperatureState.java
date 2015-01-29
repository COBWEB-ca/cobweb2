package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.plugins.AgentState;

public class TemperatureState implements AgentState {
	public float originalParamValue;

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 1L;
}