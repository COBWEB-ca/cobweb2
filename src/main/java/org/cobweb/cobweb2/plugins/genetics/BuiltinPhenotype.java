package org.cobweb.cobweb2.plugins.genetics;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.ui.config.PropertyAccessor;

/**
 * Phenotype that uses Reflection to modify fields of ComplexAgentParams
 */
public class BuiltinPhenotype extends PropertyPhenotype {

	/**
	 *
	 * @param x field to modify
	 */
	BuiltinPhenotype(PropertyAccessor x) {
		super(x);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BuiltinPhenotype) {
			BuiltinPhenotype o = (BuiltinPhenotype) obj;
			return super.equals(o);
		}
		return false;
	}

	@Override
	protected Object rootAccessor(Agent a) {
		return ((ComplexAgent) a).params;
	}

	private static final long serialVersionUID = 2L;
}
