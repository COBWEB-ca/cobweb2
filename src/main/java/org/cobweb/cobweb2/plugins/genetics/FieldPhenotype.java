package org.cobweb.cobweb2.plugins.genetics;

import java.lang.reflect.Field;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Mutatable;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.ReflectionUtil;

/**
 * Phenotype that uses Reflection to modify fields of ComplexAgentParams
 */
public class FieldPhenotype extends Phenotype {

	private Field field = null;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FieldPhenotype) {
			FieldPhenotype p = (FieldPhenotype) obj;
			if (p.field == null && this.field == null) return true;
			if (p.field == null || this.field == null) return false;
			return p.field.equals(this.field);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	/**
	 *
	 * @param f field to modify
	 */
	FieldPhenotype(Field f) {
		if (f != null &&
				(f.getAnnotation(Mutatable.class) == null || f.getAnnotation(ConfDisplayName.class) == null))
			throw new IllegalArgumentException("Field must be labeled as @Mutatable and have a @ConfDisplayName");
		this.field = f;
	}

	@Override
	public String getIdentifier() {
		return field.getAnnotation(ConfXMLTag.class).value();
	}

	@Override
	public String getName() {
		return field.getAnnotation(ConfDisplayName.class).value();
	}

	@Override
	public void modifyValue(Agent a, float m, float b) {
		if (field == null)
			return;

		Object params = getParamObject(a);
		ReflectionUtil.modifyFieldLinear(params, this.field, m, b);
	}

	protected Object getParamObject(Agent a) {
		return ((ComplexAgent) a).params;
	}

	@Override
	public float getValue(Agent a) {
		Object params = getParamObject(a);
		return ReflectionUtil.getFieldAsFloat(params, field);
	}

	@Override
	public void setValue(Agent a, float value) {
		Object params = getParamObject(a);
		ReflectionUtil.setFieldWithFloat(params, field, value);
	}

	private static final long serialVersionUID = 2L;

}
