package org.cobweb.cobweb2;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cobweb.cobweb2.abiotic.TemperatureMutator;
import org.cobweb.cobweb2.core.AgentListener;
import org.cobweb.cobweb2.core.AgentSpawner;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.EnvironmentStats;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.disease.DiseaseMutator;
import org.cobweb.cobweb2.genetics.GeneticsMutator;
import org.cobweb.cobweb2.interconnect.AgentSimilarityCalculator;
import org.cobweb.cobweb2.interconnect.MutatorListener;
import org.cobweb.cobweb2.interconnect.StateParameter;
import org.cobweb.cobweb2.interconnect.StatePlugin;
import org.cobweb.cobweb2.production.ProductionMapper;
import org.cobweb.util.RandomNoGenerator;

/**
 * This class provides the definitions for a user interface that is running
 * on a local machine.
 *
 */
public class Simulation implements SimulationInternals {

	// TODO access level?
	public ComplexEnvironment theEnvironment;

	private int time = 0;

	private TemperatureMutator tempMutator;

	public GeneticsMutator geneticMutator;

	public DiseaseMutator diseaseMutator;

	// TODO access level?
	public SimulationConfig simulationConfig;

	private RandomNoGenerator random;

	private AgentSpawner agentSpawner;

	private GeneticsMutator similarityCalculator;

	/* return number of TYPES of agents in the environment */
	@Override
	public int getAgentTypeCount() {
		return simulationConfig.getEnvParams().agentTypeCount;
	}

	@Override
	public EnvironmentStats getStatistics() {
		return theEnvironment.getStatistics();
	}

	@Override
	public long getTime() {
		return time;
	}

	public void resetTime() {
		time = 0;
	}

	/**
	 * Initialize the specified Environment class.  The environment is created using the
	 * environmentName.load method.
	 *
	 * @param environmentName Class name of the environment used in this simulation.
	 * @param p Simulation parameters that can be defined by the simulation data file (xml file).
	 */
	private void InitEnvironment(String environmentName, SimulationConfig p) {
		try {
			if (theEnvironment == null || !theEnvironment.getClass().equals(Class.forName(environmentName))) {
				Class<?> environmentClass = Class.forName(environmentName);

				Constructor<?> environmentCtor = environmentClass.getConstructor(SimulationInternals.class);
				if (environmentCtor == null)
					throw new InstantiationError("No valid constructor found on environment class.");

				theEnvironment = (ComplexEnvironment) environmentCtor.newInstance(this);
			}
			theEnvironment.load(p);
		} catch (Exception ex) {
			throw new RuntimeException("Can't InitEnvironment", ex);
		}
	}

	/**
	 * Initialize the specified environment class with state data read from the
	 * reader. It first adds the various mutators used to modify actions in the
	 * event that the agent spawns, steps, or contacts another agent.  It then
	 * uses the simulation parameters to modify the properties of the mutators.
	 * It then initializes the simulation environment (InitEnvironment) using
	 * the simulation configuration object.  Finally, it will start the scheduler,
	 * which will start the simulation.  This is a private helper to the
	 * LocalUIInterface constructor.
	 */
	@Override
	public void load(SimulationConfig p) {
		this.simulationConfig = p;
		agentSpawner = new AgentSpawner(p.getEnvParams().agentName, this);

		// TODO: this is a hack to make the applet work when we switch grids and
		// the static information is not cleared, ComplexAgent should really
		// have a way to track which mutators have been bound
		if (!p.getEnvParams().keepOldAgents) {
			mutatorListener.clearMutators();
			plugins.clear();
			agents.clear();
			geneticMutator = null;
			diseaseMutator = null;
			tempMutator = null;
			prodMapper = null;
		}

		if (geneticMutator == null) {
			geneticMutator = new GeneticsMutator(this);
			mutatorListener.addMutator(geneticMutator);
			similarityCalculator = geneticMutator;
		}
		if (diseaseMutator == null) {
			diseaseMutator = new DiseaseMutator(this);
			mutatorListener.addMutator(diseaseMutator);
		}
		if (tempMutator == null) {
			tempMutator = new TemperatureMutator();
			mutatorListener.addMutator(tempMutator);
		}
		if (prodMapper == null) {
			prodMapper = new ProductionMapper(simulationConfig.getProdParams());
			plugins.add(prodMapper);
			mutatorListener.addMutator(prodMapper);
		}

		geneticMutator.setParams(p.getGeneticParams(), p.getEnvParams().getAgentTypes());

		diseaseMutator.setParams(p.getDiseaseParams(), p.getEnvParams().getAgentTypes());

		tempMutator.setParams(p.getTempParams(), p.getEnvParams());

		random = new RandomNoGenerator(p.getEnvParams().randomSeed);


		InitEnvironment(p.getEnvParams().environmentName, p);

		prodMapper.setParams(
				this,
				simulationConfig.getProdParams(),
				p.getEnvParams().keepOldWaste);

		setupPlugins();
	}

	@Override
	public void step() {

		theEnvironment.update(time);

		// TODO synchronize on something other than environment?
		synchronized(theEnvironment) {
			for (ComplexAgent agent : new LinkedList<ComplexAgent>(agents)) {
				agent.update(time);

				prodMapper.tryProduction(agent);

				if (!agent.isAlive())
					agents.remove(agent);
			}
			diseaseMutator.update(time);
			// TODO update other modules here
		}

		time++;
	}

	private List<ComplexAgent> agents = new LinkedList<ComplexAgent>();

	@Override
	public void addAgent(ComplexAgent agent) {
		agents.add(agent);
	}

	@Override
	public RandomNoGenerator getRandom() {
		return random;
	}

	@Override
	public ComplexAgent newAgent() {
		return agentSpawner.spawn();
	}

	@Override
	public Environment getEnvironment() {
		return theEnvironment;
	}

	@Override
	public StateParameter getStateParameter(String name) {
		return pluginMap.get(name);
	}

	private Map<String, StateParameter> pluginMap = new LinkedHashMap<String, StateParameter>();

	private List<StatePlugin> plugins = new LinkedList<StatePlugin>();

	private void setupPlugins() {
		for (StatePlugin plugin : plugins) {
			for (StateParameter param : plugin.getParameters()) {
				pluginMap.put(param.getName(), param);
			}
		}
	}

	@Override
	public Set<String> getStatePlugins() {
		return pluginMap.keySet();
	}

	public ProductionMapper prodMapper;

	@Override
	public AgentSimilarityCalculator getSimilarityCalculator() {
		return similarityCalculator;
	}

	public MutatorListener mutatorListener = new MutatorListener();

	@Override
	public AgentListener getAgentListener() {
		return mutatorListener;
	}

}
