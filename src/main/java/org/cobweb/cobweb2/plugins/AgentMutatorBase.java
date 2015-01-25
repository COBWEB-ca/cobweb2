package org.cobweb.cobweb2.plugins;

import java.util.HashMap;
import java.util.Map;

import org.cobweb.cobweb2.core.Agent;


public abstract class AgentMutatorBase<T> implements AgentMutator {

	private Map<Agent, T> agentStates = new HashMap<>();

	/**
	 * Gets state of agent associated with this mutator.
	 * If no state stored, attempts to create by calling createState.
	 *
	 * @param agent agent to get state for
	 * @return state object for agent, null if state not stored intentionally.
	 */
	protected T getAgentState(Agent agent) {
		T result = null;
		if (!agentStates.containsKey(agent)) {
			result = createState(agent);
			if (result == null)
				return null;
			agentStates.put(agent, result);
		} else {
			result = agentStates.get(agent);
		}

		return result;
	}

	protected void clearAgentStates() {
		agentStates.clear();
	}

	/**
	 * Creates state object for agent
	 * @param agent agent to attach state to
	 * @return new state object to attach to agent, if null, state will not be attached
	 */
	protected abstract T createState(Agent agent);

}
