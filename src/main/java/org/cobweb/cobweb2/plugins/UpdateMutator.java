package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;


public interface UpdateMutator extends AgentMutator {

	public void onUpdate(Agent agent);
}
