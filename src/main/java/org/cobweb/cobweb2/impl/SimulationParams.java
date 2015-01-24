package org.cobweb.cobweb2.impl;

import java.util.List;


public interface SimulationParams {
	public List<String> getPluginParameters();

	public AgentFoodCountable getCounts();
}
