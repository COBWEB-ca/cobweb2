package org.cobweb.cobweb2;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.AgentListener;
import org.cobweb.cobweb2.core.AgentSimilarityCalculator;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.StateParameter;
import org.cobweb.cobweb2.core.StatePlugin;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.impl.AgentSpawner;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.cobweb2.plugins.MutatorListener;
import org.cobweb.cobweb2.plugins.abiotic.TemperatureMutator;
import org.cobweb.cobweb2.plugins.disease.DiseaseMutator;
import org.cobweb.cobweb2.plugins.genetics.GeneticsMutator;
import org.cobweb.cobweb2.plugins.production.ProductionMapper;
import org.cobweb.cobweb2.plugins.waste.WasteMutator;
import org.cobweb.cobweb2.ui.SimulationInterface;
import org.cobweb.util.RandomNoGenerator;

/**
 * This class provides the definitions for a user interface that is running
 * on a local machine.
 *
 */
public class Simulation implements SimulationInternals, SimulationInterface {

	// TODO access level?
	public ComplexEnvironment theEnvironment;

	private int time = 0;

	private TemperatureMutator tempMutator;

	// TODO access level
	public GeneticsMutator geneticMutator;

	private DiseaseMutator diseaseMutator;

	private WasteMutator wasteMutator;

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
		} catch (InstantiationError | ClassNotFoundException | NoSuchMethodException |
				InstantiationException | IllegalAccessException | InvocationTargetException ex) {
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
			wasteMutator = null;
		}

		if (geneticMutator == null) {
			geneticMutator = new GeneticsMutator();
			mutatorListener.addMutator(geneticMutator);
			similarityCalculator = geneticMutator;
		}
		if (diseaseMutator == null) {
			diseaseMutator = new DiseaseMutator();
			mutatorListener.addMutator(diseaseMutator);
		}
		if (tempMutator == null) {
			tempMutator = new TemperatureMutator();
			plugins.add(tempMutator);
			mutatorListener.addMutator(tempMutator);
		}
		if (prodMapper == null) {
			prodMapper = new ProductionMapper(this);
			plugins.add(prodMapper);
			mutatorListener.addMutator(prodMapper);
		}
		if (wasteMutator == null) {
			wasteMutator = new WasteMutator(this);
			mutatorListener.addMutator(wasteMutator);
		}

		geneticMutator.setParams(this, p.getGeneticParams(), p.getEnvParams().getAgentTypes());

		diseaseMutator.setParams(this, p.getDiseaseParams(), p.getEnvParams().getAgentTypes());

		tempMutator.setParams(p.getTempParams(), p.getEnvParams());

		wasteMutator.setParams(p.getWasteParams());

		prodMapper.setParams(simulationConfig.getProdParams());

		// 0 = use default seed
		if (p.getEnvParams().randomSeed == 0)
			random = new RandomNoGenerator();
		else
			random = new RandomNoGenerator(p.getEnvParams().randomSeed);


		InitEnvironment(p.getEnvParams().environmentName, p);

		prodMapper.initEnvironment(theEnvironment, p.getEnvParams().keepOldWaste);

		wasteMutator.initEnvironment(theEnvironment, p.getEnvParams().keepOldWaste);

		setupPlugins();
	}

	@Override
	public void step() {

		theEnvironment.update();

		// TODO synchronize on something other than environment?
		synchronized(theEnvironment) {
			for (Agent agent : new LinkedList<Agent>(agents)) {
				agent.update();

				mutatorListener.onUpdate(agent);

				if (!agent.isAlive())
					agents.remove(agent);
			}
			diseaseMutator.update();
			// TODO update other modules here
		}

		time++;
	}

	private List<Agent> agents = new LinkedList<Agent>();

	@Override
	public void addAgent(Agent agent) {
		agents.add(agent);
	}

	@Override
	public RandomNoGenerator getRandom() {
		return random;
	}

	@Override
	public Agent newAgent(int type) {
		ComplexAgent agent = (ComplexAgent) agentSpawner.spawn();
		agent.setController(simulationConfig.getControllerParams().createController(this, type));
		return agent;
	}

	@Override
	public Topology getTopology() {
		return theEnvironment.topology;
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
	public Set<String> getStatePluginKeys() {
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
