package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.impl.ComplexAgent;

/**
 * Helper base class for creating StatefulMutators.
 * Handles storage of state.
 */
public abstract class StatefulMutatorBase<T extends AgentState> implements StatefulMutator<T> {

	private final Class<T> stateClass;

	protected StatefulMutatorBase(Class<T> stateClass) {
		this.stateClass = stateClass;
	}

	@Override
	public T getAgentState(Agent agent) {
		T result = ((ComplexAgent)agent).getState(getStateClass());
		return result;
	}

	@Override
	public boolean hasAgentState(Agent agent) {
		return getAgentState(agent) != null;
	}

	protected T removeAgentState(Agent agent) {
		return ((ComplexAgent)agent).removeState(getStateClass());
	}

	protected void setAgentState(Agent agent, T state) {
		((ComplexAgent)agent).setState(getStateClass(), state);
	}

	@Override
	public Class<T> getStateClass() {
		return stateClass;
	}

	/**
	 * Checks whether the given AgentState is valid for this mutator in the current simulation configuration
	 */
	protected abstract boolean validState(T value);

	@SuppressWarnings("unchecked")
	@Override
	public <U extends AgentState> boolean acceptsState(Class<U> type, U value) {
		if (stateClass.equals(type)) {
			return validState((T) value);
		}
		return false;
	}
}
