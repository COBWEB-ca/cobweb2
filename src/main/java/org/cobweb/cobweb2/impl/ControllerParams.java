package org.cobweb.cobweb2.impl;

import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.plugins.ResizableParam;

/**
 * Configuration for a Controller
 */
public interface ControllerParams extends ResizableParam {

	/**
	 * Creates Controller for given agent type
	 * @param sim simulation the agent is in
	 * @param type agent type
	 * @return Controller for agent
	 */
	public Controller createController(SimulationInternals sim, int type);
}
