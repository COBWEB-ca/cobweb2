package org.cobweb.cobweb2.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cobweb.cobweb2.core.Agent;

/**
 * Helper base class for creating StatefulMutators.
 * Handles storage of state.
 */
public class StatefulMutatorBase<T> implements StatefulMutator<T> {

	protected Map<Agent, T> agentStates = new HashMap<>();
	private final Class<T> stateClass;

	protected StatefulMutatorBase(Class<T> stateClass) {
		this.stateClass = stateClass;
	}

	@Override
	public T getAgentState(Agent agent) {
		T result = agentStates.get(agent);
		return result;
	}

	@Override
	public boolean hasAgentState(Agent agent) {
		return agentStates.containsKey(agent);
	}

	protected void clearAgentStates() {
		agentStates.clear();
	}

	protected T removeAgentState(Agent agent) {
		return agentStates.remove(agent);
	}

	protected void setAgentState(Agent agent, T state) {
		agentStates.put(agent, state);
	}

	protected Set<Agent> getAgentsWithState() {
		return agentStates.keySet();
	}

	@Override
	public Class<T> getStateClass() {
		return stateClass;
	}

}
