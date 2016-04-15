package org.cobweb.cobweb2.plugins.genetics;

import java.lang.reflect.Field;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.ui.config.FieldPropertyAccessor;

/**
 * Phenotype that uses Reflection to modify fields of ComplexAgentParams
 */
public class FieldPhenotype extends PropertyPhenotype {

	/**
	 *
	 * @param f field to modify
	 */
	FieldPhenotype(Field f) {
		super(new FieldPropertyAccessor(f));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FieldPhenotype) {
			FieldPhenotype o = (FieldPhenotype) obj;
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
