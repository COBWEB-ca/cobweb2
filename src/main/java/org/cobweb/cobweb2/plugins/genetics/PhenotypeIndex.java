package org.cobweb.cobweb2.plugins.genetics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.impl.ComplexAgentParams;
import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.cobweb2.ui.config.FieldPropertyAccessor;
import org.cobweb.cobweb2.ui.config.PropertyAccessor;
import org.cobweb.cobweb2.ui.config.SetterPropertyAccessor;
import org.cobweb.util.MutatableFloat;
import org.cobweb.util.MutatableInt;
import org.reflections.Reflections;


public class PhenotypeIndex {

	public static Set<Phenotype> getPossibleValues() {
		Set<Phenotype> bindables = new LinkedHashSet<Phenotype>();

		// Null phenotype
		bindables.add(new NullPhenotype());

		// ComplexAgentParams phenotypes
		for (PropertyAccessor p : classGetProperties(ComplexAgentParams.class)) {
			bindables.add(new BuiltinPhenotype(p));
		}

		// Plugin phenotypes
		Reflections pluginsPackage = new Reflections("org.cobweb.cobweb2.plugins");
		SortedSet<Class<? extends AgentState>> orderedClasses = new TreeSet<>(new PluginOrderComparator());
		orderedClasses.addAll(pluginsPackage.getSubTypesOf(AgentState.class));

		for (Class<? extends AgentState> stateClass : orderedClasses)
		{
			try {
				Field agentParamField = stateClass.getField("agentParams");
				FieldPropertyAccessor stateAccessor = new FieldPropertyAccessor(agentParamField);
				Class<?> agentParamType = stateAccessor.getType();

				for (PropertyAccessor p : classGetProperties(agentParamType)) {
					bindables.add(new PluginPhenotype(stateClass, stateAccessor, p));
				}

			} catch (NoSuchFieldException ex) {
				// This AgentState doesn't have an agentParams
				continue;
			} catch (SecurityException ex) {
				throw new RuntimeException("Unable to configure phenotypes for " + stateClass.getSimpleName(), ex);
			}
		}

		return Collections.unmodifiableSet(bindables);
	}

	private static Collection<PropertyAccessor> classGetProperties(Class<?> clazz) {
		List<PropertyAccessor> res = new ArrayList<>();
		for (Method p: clazz.getMethods()) {
			if (MutatableFloat.class.isAssignableFrom(p.getReturnType()) ||
					MutatableInt.class.isAssignableFrom(p.getReturnType()))
				res.add(new SetterPropertyAccessor(p));
		}
		for (Field p: clazz.getFields()) {
			if (MutatableFloat.class.isAssignableFrom(p.getType()) ||
					MutatableInt.class.isAssignableFrom(p.getType()))
				res.add(new FieldPropertyAccessor(p));
		}
		return res;
	}

	private static class PluginOrderComparator implements Comparator<Class<?>> {
		@Override
		public int compare(Class<?> o1, Class<?> o2) {
			return o1.getSimpleName().compareTo(o2.getSimpleName());
		}
	}
}
