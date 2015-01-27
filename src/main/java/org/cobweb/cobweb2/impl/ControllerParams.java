package org.cobweb.cobweb2.impl;

import org.cobweb.cobweb2.core.Controller;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.plugins.PerTypeParam;
import org.cobweb.io.ParameterSerializable;

/**
 * Configuration for a Controller
 */
public interface ControllerParams extends ParameterSerializable, PerTypeParam {

	/**
	 * Creates Controller for given agent type
	 * @param sim simulation the agent is in
	 * @param type agent type
	 * @return Controller for agent
	 */
	public Controller createController(SimulationInternals sim, int type);
}
