package org.cobweb.cobweb2.impl;

import java.util.List;

import org.cobweb.cobweb2.core.AgentFoodCountable;


public interface SimulationParams extends AgentFoodCountable {
	public List<String> getPluginParameters();
}
