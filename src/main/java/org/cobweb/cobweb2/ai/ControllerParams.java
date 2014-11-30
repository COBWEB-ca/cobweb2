package org.cobweb.cobweb2.ai;

import org.cobweb.cobweb2.interconnect.AgentFoodCountable;
import org.cobweb.cobweb2.io.CobwebParam;


public interface ControllerParams extends CobwebParam {

	public void setTypeCount(int count);

	public void resize(AgentFoodCountable envParams);

}
