package org.cobweb.cobweb2.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.io.Cobweb2Serializer;
import org.cobweb.cobweb2.io.Cobweb2Serializer.AgentSample;
import org.cobweb.cobweb2.plugins.AgentState;


public class PopulationSampler {


	/** Save a sample population as an XML file */
	public static void savePopulation(Simulation sim, String popName, int totalPop) {

		Cobweb2Serializer serializer = new Cobweb2Serializer();

		List<Agent> allAgents = new ArrayList<>(sim.theEnvironment.getAgents());
		Collections.shuffle(allAgents);
		List<Agent> sampleAgents = allAgents.subList(0, totalPop);

		try (OutputStream outputStream = new FileOutputStream(popName)) {
			serializer.serializeAgents(sampleAgents, outputStream);

		} catch (IOException ex) {
			throw new RuntimeException("Could not save population", ex);
		}
	}

	/**
	 * Checks if given population file is compatible with current simulation settings
	 * @param fileName path to population file
	 * @return list of AgentState types incompatible with current simulation, empty set if all compatible
	 */
	public static Set<String> checkPopulationCompatible(Simulation sim, String fileName) {
		try (InputStream inFile = new FileInputStream(fileName)) {
			Cobweb2Serializer serializer = new Cobweb2Serializer();
			Collection<AgentSample> agents = serializer.loadAgents(inFile, sim.simulationConfig);
			if (agents.size() == 0)
				throw new UserInputException("No agents saved in this file");

			// Check if simulation parameters are consistent with sample population
			Set<String> unsupported = new HashSet<>();
			for (AgentSample agentSample : agents) {
				if (agentSample.type >= sim.getAgentTypeCount())
					unsupported.add("Number of agent types");

				for (Entry<Class<AgentState>, AgentState> pluginState : agentSample.plugins.entrySet()) {
					if (!sim.supportsState(pluginState.getKey(), pluginState.getValue())) {
						unsupported.add(pluginState.getKey().getSimpleName());
					}
				}
			}
			return unsupported;
		} catch (IOException ex) {
			throw new RuntimeException("Can't open population file", ex);
		}
	}

	/**
	 * Loads agent population saved with savePopulation()
	 * @param fileName path to population file
	 * @param replace delete current population before inserting
	 */
	public static void insertPopulation(Simulation sim, String fileName, boolean replace) {
		if (replace) {
			sim.theEnvironment.clearAgents();
		}

		try (InputStream inFile = new FileInputStream(fileName)) {
			Cobweb2Serializer serializer = new Cobweb2Serializer();
			Collection<AgentSample> agents = serializer.loadAgents(inFile, sim.simulationConfig);


			for (AgentSample agentSample : agents) {
				if (agentSample.type >= sim.getAgentTypeCount())
					continue;

				ComplexAgent cAgent = (ComplexAgent) sim.newAgent(agentSample.type);
				cAgent.init(sim.theEnvironment, agentSample.position, agentSample.params);
				for (Entry<Class<AgentState>, AgentState> pluginState : agentSample.plugins.entrySet()) {
					if (sim.supportsState(pluginState.getKey(), pluginState.getValue())) {
						cAgent.setState(pluginState.getKey(), pluginState.getValue());
					}
				}
			}

		} catch (IOException ex) {
			throw new UserInputException("Can't open population file", ex);
		}
	}

}
