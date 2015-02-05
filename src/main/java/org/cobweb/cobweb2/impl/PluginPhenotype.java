package org.cobweb.cobweb2.impl;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.cobweb2.ui.config.FieldPropertyAccessor;
import org.cobweb.cobweb2.ui.config.PropertyAccessor;



public class PluginPhenotype extends Phenotype {

	private Class<? extends AgentState> type;
	private PropertyAccessor propertyAccessor;
	private PropertyAccessor stateParamAccessor;

	public PluginPhenotype(Class<? extends AgentState> type, FieldPropertyAccessor stateParamAccessor,
			PropertyAccessor propertyAccessor) {
		this.type = type;
		this.stateParamAccessor = stateParamAccessor;
		this.propertyAccessor = propertyAccessor;
	}

	protected Object getParamObject(Agent a) {
		AgentState state = ((ComplexAgent) a).getState(type);
		if (state == null)
			return null;

		return stateParamAccessor.get(state);
	}


	private static final long serialVersionUID = 2L;


	@Override
	public String getIdentifier() {
		return propertyAccessor.getXmlName();
	}

	@Override
	public String getName() {
		return propertyAccessor.getName();
	}

	@Override
	public void modifyValue(Agent a, float m, float b) {
		Object state = getParamObject(a);
		if (state == null)
			return;
		setValue(a, getValue(a) * m + b);
	}

	@Override
	public float getValue(Agent a) {
		Object state = getParamObject(a);
		if (state == null)
			return 0;

		Object v = propertyAccessor.get(state);
		if (propertyAccessor.getType().equals(float.class))
			return (float)v;
		if (propertyAccessor.getType().equals(int.class))
			return (int)v ;
		else
			throw new IllegalArgumentException("Field is not one of the acceptible types");
	}

	@Override
	public void setValue(Agent a, float value) {
		Object state = getParamObject(a);
		if (state == null)
			return;

		if (propertyAccessor.getType().equals(float.class)) {
			propertyAccessor.set(state, value);
		} else if (propertyAccessor.getType().equals(int.class)) {
			propertyAccessor.set(state, Math.round(value));
		} else {
			throw new IllegalArgumentException("Field is not one of the acceptible types");
		}
	}
}
