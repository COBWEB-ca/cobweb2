package org.cobweb.cobweb2.impl;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.cobweb.cobweb2.core.Mutatable;
import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.plugins.pd.PDAgentParams;
import org.cobweb.cobweb2.plugins.pd.PDState;
import org.cobweb.cobweb2.ui.config.FieldPropertyAccessor;


public class PhenotypeIndex {

	public static Set<Phenotype> getPossibleValues() {
		Set<Phenotype> bindables = new LinkedHashSet<Phenotype>();

		// Null phenotype
		bindables.add(new NullPhenotype());

		// ComplexAgentParams phenotypes
		for (Field f: ComplexAgentParams.class.getFields()) {
			if (f.getAnnotation(Mutatable.class) != null)
				bindables.add(new FieldPhenotype(f));
		}

		// PD phenotypes
		try {
			FieldPropertyAccessor stateAccessor = new FieldPropertyAccessor(PDState.class.getField("agentParams"));
			for (Field f : PDAgentParams.class.getFields()) {
				if (f.isAnnotationPresent(Mutatable.class))
					bindables.add(new PluginPhenotype(PDState.class, stateAccessor, new FieldPropertyAccessor(f)));
			}
		} catch (NoSuchFieldException | SecurityException ex) {
			throw new RuntimeException("Unable to configure PD phenotypes", ex);
		}

		return Collections.unmodifiableSet(bindables);
	}
}
