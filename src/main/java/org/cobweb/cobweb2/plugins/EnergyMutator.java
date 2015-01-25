package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;


public interface EnergyMutator extends AgentMutator {

	public void onEnergyChange(Agent agent, int delta);
}
