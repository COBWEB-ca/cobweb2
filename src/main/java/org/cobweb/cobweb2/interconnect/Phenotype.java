package org.cobweb.cobweb2.interconnect;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterChoice;
import org.cobweb.util.ReflectionUtil;

public class Phenotype implements ParameterChoice {

	private static final long serialVersionUID = -6142169580857190598L;

	public Field field = null;

	public Phenotype() {
		// Empty constructor for Null phenotype
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Phenotype) {
			Phenotype p = (Phenotype) obj;
			if (p.field == null && this.field == null) return true;
			if (p.field == null || this.field == null) return false;
			return p.field.equals(this.field);
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (field == null)
			return 0;

		return field.hashCode();
	}

	/**
	 *
	 * @param f field to modify
	 */
	public Phenotype(Field f) {
		if (f != null &&
				(f.getAnnotation(GeneMutatable.class) == null || f.getAnnotation(ConfDisplayName.class) == null))
			throw new IllegalArgumentException("Field must be labeled as @GeneMutatable and have a @ConfDisplayName");
		this.field = f;
	}

	@Override
	public String getIdentifier() {
		if (field == null)
			return "None";

		return field.getAnnotation(ConfXMLTag.class).value();
	}

	@Override
	public String toString() {
		if (field == null)
			return "[Null]";

		return field.getAnnotation(ConfDisplayName.class).value();
	}

	public void modifyValue(Agent a, float m, float b) {
		if (field == null)
			return;

		ComplexAgentParams params = ((ComplexAgent) a).params;
		ReflectionUtil.modifyFieldLinear(params, this.field, m, b);
	}


	public static Set<Phenotype> getPossibleValues() {
		Set<Phenotype> bindables = new LinkedHashSet<Phenotype>();
		// Null phenotype
		bindables.add(new Phenotype());

		// @GeneMutatable fields
		for (Field f: ComplexAgentParams.class.getFields()) {
			if (f.getAnnotation(GeneMutatable.class) != null)
				bindables.add(new Phenotype(f));
		}
		return Collections.unmodifiableSet(bindables);
	}
}