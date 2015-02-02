package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;


public interface EnergyMutator extends AgentMutator {

	public void onEnergyChange(Agent agent, int delta, Cause cause);
}
