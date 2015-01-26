package org.cobweb.cobweb2.plugins;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.cobweb.cobweb2.impl.AgentFoodCountable;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;


public abstract class PerAgentParams<T extends ParameterSerializable> implements ParameterSerializable {

	@ConfXMLTag("AgentParams")
	@ConfList(indexName = "Agent", startAtOne = true)
	public T[] agentParams;

	private Class<T> agentParamClass;

	/**
	 * Creates agent param array of size 0. Call resize() to set required size after.
	 */
	@SuppressWarnings("unchecked")
	public PerAgentParams(Class<T> agentParamClass) {
		this.agentParamClass = agentParamClass;
		agentParams = (T[]) Array.newInstance(this.agentParamClass, 0);
	}

	/**
	 * Create agent param array and resize to initial size.
	 * Requires that newAgentParam() does not depend on any new members of the subclass!
	 */
	public PerAgentParams(Class<T> agentparClass, AgentFoodCountable initialSize) {
		this(agentparClass);
		resize(initialSize);
	}

	public void resize(AgentFoodCountable envParams) {
		T[] n = Arrays.copyOf(agentParams, envParams.getAgentTypes());

		for (int i = agentParams.length; i < envParams.getAgentTypes(); i++) {
			n[i] = newAgentParam();
		}
		agentParams = n;
	}

	protected abstract T newAgentParam();

	private static final long serialVersionUID = 1L;
}
