package org.cobweb.cobweb2.compatibility;

import org.cobweb.cobweb2.ai.GeneticController;
import org.cobweb.cobweb2.ai.LinearWeightsController;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.params.ComplexEnvironmentParams;
import org.cobweb.cobweb2.eventlearning.ComplexAgentLearning;
import org.cobweb.cobweb2.eventlearning.ComplexEnvironmentLearning;


public class ConfigUpgrader {

	public static void upgrade(ComplexEnvironmentParams envParams) {
		envParams.controllerName = updateClassName(
				envParams.controllerName,
				GeneticController.class,
				LinearWeightsController.class
				);

		envParams.agentName = updateClassName(
				envParams.agentName,
				ComplexAgent.class,
				ComplexAgentLearning.class
				);

		envParams.environmentName = updateClassName(
				envParams.environmentName,
				ComplexEnvironment.class,
				ComplexEnvironmentLearning.class
				);
	}

	private static String updateClassName(String oldName, Class<?>... candidates) {
		try {
			Class.forName(oldName);
			return oldName;
		} catch (ClassNotFoundException ex) {
			for (Class<?> c : candidates) {
				if (oldName.endsWith("." + c.getSimpleName())) {
					return c.getName();
				}
			}
		}

		throw new IllegalArgumentException("Cannot find missing class: " + oldName);
	}

}
