package org.cobweb.cobweb2.impl;

import java.util.List;

import org.cobweb.cobweb2.core.AgentFoodCountable;


public interface SimulationParams {
	public List<String> getPluginParameters();

	public AgentFoodCountable getCounts();
}
