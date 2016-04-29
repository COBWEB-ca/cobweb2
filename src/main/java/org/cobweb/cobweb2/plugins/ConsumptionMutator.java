package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;


public interface ConsumptionMutator extends AgentMutator {

	public void onConsumeAgent(Agent agent, Agent food);

	public void onConsumeFood(Agent agent, int foodType);
}
