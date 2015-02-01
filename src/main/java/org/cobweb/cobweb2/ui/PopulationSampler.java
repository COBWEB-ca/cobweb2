package org.cobweb.cobweb2.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.io.Cobweb2Serializer;
import org.cobweb.cobweb2.io.Cobweb2Serializer.AgentSample;


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
				ComplexAgent cAgent = (ComplexAgent) sim.newAgent(agentSample.type);
				cAgent.init(sim.theEnvironment, agentSample.position, agentSample.params);
			}

		} catch (IOException ex) {
			throw new RuntimeException("Can't open population file", ex);
		}
	}

}
