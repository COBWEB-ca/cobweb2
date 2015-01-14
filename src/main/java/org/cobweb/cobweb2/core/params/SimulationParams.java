package org.cobweb.cobweb2.core.params;

import java.util.List;


public interface SimulationParams {
	public List<String> getPluginParameters();

	public AgentFoodCountable getCounts();
}
