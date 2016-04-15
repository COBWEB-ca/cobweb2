package org.cobweb.cobweb2.plugins.genetics;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.cobweb.cobweb2.core.Mutatable;
import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.impl.ComplexAgentParams;
import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.cobweb2.plugins.abiotic.AbioticState;
import org.cobweb.cobweb2.plugins.disease.DiseaseState;
import org.cobweb.cobweb2.plugins.pd.PDState;
import org.cobweb.cobweb2.plugins.production.ProductionState;
import org.cobweb.cobweb2.plugins.waste.WasteState;
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


		for (Class<? extends AgentState> stateClass : Arrays.asList(
				AbioticState.class, DiseaseState.class, PDState.class, ProductionState.class, WasteState.class
				))
		{
			try {
				Field agentParamField = stateClass.getField("agentParams");
				FieldPropertyAccessor stateAccessor = new FieldPropertyAccessor(agentParamField);
				Class<?> agentParamType = stateAccessor.getType();

				for (Field f: agentParamType.getFields()) {
					if (f.isAnnotationPresent(Mutatable.class))
						bindables.add(new PluginPhenotype(stateClass, stateAccessor, new FieldPropertyAccessor(f)));
				}
			} catch (NoSuchFieldException ex) {
				continue;
			} catch (SecurityException ex) {
				throw new RuntimeException("Unable to configure phenotypes for " + stateClass.getSimpleName(), ex);
			}
		}

		return Collections.unmodifiableSet(bindables);
	}
}
