package org.cobweb.cobweb2.plugins.genetics;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.cobweb2.ui.config.PropertyAccessor;



public class PluginPhenotype extends PropertyPhenotype {

	private Class<? extends AgentState> type;
	private PropertyAccessor stateParamAccessor;

	public PluginPhenotype(Class<? extends AgentState> type,
			PropertyAccessor stateParamAccessor,
			PropertyAccessor propertyAccessor) {
		super(propertyAccessor);
		this.type = type;
		this.stateParamAccessor = stateParamAccessor;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PluginPhenotype) {
			PluginPhenotype o = (PluginPhenotype) obj;
			return o.type.equals(this.type) &&
					o.stateParamAccessor.equals(this.stateParamAccessor) &&
					super.equals(o);
		}
		return false;
	}

	@Override
	protected Object rootAccessor(Agent a) {
		AgentState state = ((ComplexAgent) a).getState(type);
		if (state == null)
			return null;

		return stateParamAccessor.get(state);
	}


	private static final long serialVersionUID = 2L;
}
