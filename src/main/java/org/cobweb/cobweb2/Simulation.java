package org.cobweb.cobweb2;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
import org.cobweb.cobweb2.plugins.abiotic.AbioticMutator;
import org.cobweb.cobweb2.plugins.broadcast.PacketConduit;
import org.cobweb.cobweb2.plugins.disease.DiseaseMutator;
import org.cobweb.cobweb2.plugins.food.FoodGrowth;
import org.cobweb.cobweb2.plugins.genetics.GeneticsMutator;
import org.cobweb.cobweb2.plugins.pd.PDMutator;
import org.cobweb.cobweb2.plugins.production.ProductionMapper;
import org.cobweb.cobweb2.plugins.stats.StatsMutator;
import org.cobweb.cobweb2.plugins.vision.VisionMutator;
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

	private AbioticMutator abioticMutator;

	// TODO access level
	public GeneticsMutator geneticMutator;

	private DiseaseMutator diseaseMutator;

	private WasteMutator wasteMutator;

	public StatsMutator statsMutator;

	public SimulationConfig simulationConfig;

	private RandomNoGenerator random;

	private AgentSpawner agentSpawner;

	private GeneticsMutator similarityCalculator;

	private VisionMutator vision;

	private PDMutator pdMutator;

	/* return number of TYPES of agents in the environment */
	@Override
	public int getAgentTypeCount() {
		return simulationConfig.getAgentTypes();
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
	 */
	private void InitEnvironment(String environmentName, boolean continuation) {
		try {
			@SuppressWarnings("unchecked")
			Class<ComplexEnvironment> environmentClass = (Class<ComplexEnvironment>) Class.forName(environmentName);

			if (continuation && theEnvironment != null && !theEnvironment.getClass().equals(environmentClass)) {
				throw new IllegalArgumentException("Cannot switch to different Environment/Agent type and continue simulation");
			}

			if (!continuation || theEnvironment == null) {
				theEnvironment = instantiateUsingSimconfig(environmentClass);
			}
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException ex) {
			throw new RuntimeException("Can't create Environment", ex);
		}
	}

	private <T> T instantiateUsingSimconfig(Class<T> clazz)
			throws IllegalAccessException, InstantiationException, InvocationTargetException {

		Constructor<?> ctor = clazz.getConstructors()[0];

		List<Object> args = new ArrayList<>();
		for (Class<?> pt : ctor.getParameterTypes()) {
			if (pt.isAssignableFrom(this.getClass())) {
				args.add(this);
			} else {
				Object arg = simulationConfig.getParam(pt);
				if (arg == null)
					throw new IllegalArgumentException("Could not bind argument type: " + pt.getName());
				args.add(arg);
			}
		}
		@SuppressWarnings("unchecked")
		T instance = (T) ctor.newInstance(args.toArray());
		return instance;
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
		agentSpawner = new AgentSpawner(p.agentName, this);

		//TODO use reflection to automate this
		if (!p.keepOldAgents) {
			nextAgentId = 1;
			mutatorListener.clearMutators();
			plugins.clear();
			agents.clear();
			geneticMutator = null;
			diseaseMutator = null;
			abioticMutator = null;
			prodMapper = null;
			wasteMutator = null;
			statsMutator = null;
			vision = null;
			pdMutator = null;
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
		if (abioticMutator == null) {
			abioticMutator = new AbioticMutator();
			plugins.add(abioticMutator);
			mutatorListener.addMutator(abioticMutator);
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
		if (statsMutator == null) {
			statsMutator = new StatsMutator(this);
			mutatorListener.addMutator(statsMutator);
		}
		if (vision == null) {
			vision = new VisionMutator();
			mutatorListener.addMutator(vision);
		}
		if (pdMutator == null) {
			pdMutator = new PDMutator(this);
			mutatorListener.addMutator(pdMutator);
		}

		// 0 = use default seed
		if (p.randomSeed == 0)
			random = new RandomNoGenerator();
		else
			random = new RandomNoGenerator(p.randomSeed);

		InitEnvironment(p.environmentName, p.isContinuation());
		theEnvironment.load(p, p.keepOldAgents, p.keepOldArray, p.keepOldDrops);
		if (!p.isContinuation()) {
			theEnvironment.addPlugin(new FoodGrowth(this));
			theEnvironment.addPlugin(new PacketConduit());
		} else {
			PacketConduit packetConduit = theEnvironment.getPlugin(PacketConduit.class);
			if (!p.keepOldPackets)
				packetConduit.clearPackets();
		}

		geneticMutator.setParams(this, p.geneticParams, p.getAgentTypes());

		diseaseMutator.setParams(this, p.diseaseParams, p.getAgentTypes());

		abioticMutator.setParams(p.abioticParams, theEnvironment.topology);

		wasteMutator.setParams(p.wasteParams);

		prodMapper.setParams(simulationConfig.prodParams);

		pdMutator.setParams(p.pdParams);

		prodMapper.initEnvironment(theEnvironment, p.keepOldDrops);

		wasteMutator.initEnvironment(theEnvironment);

		setupPlugins();

		theEnvironment.loadNew();

		FoodGrowth foodGrowth = theEnvironment.getPlugin(FoodGrowth.class);
		foodGrowth.initEnvironment(theEnvironment, p.foodParams);

		PacketConduit packetConduit = theEnvironment.getPlugin(PacketConduit.class);
		packetConduit.initEnvironment(theEnvironment.topology);

		if (p.spawnNewAgents) {
			theEnvironment.loadNewAgents();
		}
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
		}

		time++;
	}

	private List<Agent> agents = new LinkedList<Agent>();

	private int nextAgentId = 1;

	@Override
	public void addAgent(Agent agent) {
		agents.add(agent);
		agent.id = nextAgentId++;
	}

	@Override
	public RandomNoGenerator getRandom() {
		return random;
	}

	@Override
	public Agent newAgent(int type) {
		ComplexAgent agent = (ComplexAgent) agentSpawner.spawn(type);
		agent.setController(simulationConfig.controllerParams.createController(this, type));
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
		pluginMap.clear();
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
