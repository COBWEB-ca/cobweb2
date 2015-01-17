package org.cobweb.cobweb2.interconnect;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.ReflectionUtil;

/**
 * Phenotype that uses Reflection to modify fields of ComplexAgentParams
 */
public class FieldPhenotype extends Phenotype {

	public Field field = null;

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
	public FieldPhenotype(Field f) {
		if (f != null &&
				(f.getAnnotation(GeneMutatable.class) == null || f.getAnnotation(ConfDisplayName.class) == null))
			throw new IllegalArgumentException("Field must be labeled as @GeneMutatable and have a @ConfDisplayName");
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

		ComplexAgentParams params = ((ComplexAgent) a).params;
		ReflectionUtil.modifyFieldLinear(params, this.field, m, b);
	}

	public static Set<Phenotype> getPossibleValues() {
		Set<Phenotype> bindables = new LinkedHashSet<Phenotype>();
		for (Field f: ComplexAgentParams.class.getFields()) {
			if (f.getAnnotation(GeneMutatable.class) != null)
				bindables.add(new FieldPhenotype(f));
		}
		return Collections.unmodifiableSet(bindables);
	}
}
