package org.cobweb.cobweb2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.cobweb.cobweb2.impl.AgentParams;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.cobweb2.impl.ComplexEnvironmentParams;
import org.cobweb.cobweb2.impl.ControllerParams;
import org.cobweb.cobweb2.impl.SimulationParams;
import org.cobweb.cobweb2.impl.ai.GeneticController;
import org.cobweb.cobweb2.impl.ai.GeneticControllerParams;
import org.cobweb.cobweb2.impl.learning.LearningParams;
import org.cobweb.cobweb2.plugins.ResizableParam;
import org.cobweb.cobweb2.plugins.abiotic.AbioticParams;
import org.cobweb.cobweb2.plugins.disease.DiseaseParams;
import org.cobweb.cobweb2.plugins.food.FoodGrowthParams;
import org.cobweb.cobweb2.plugins.genetics.GeneticParams;
import org.cobweb.cobweb2.plugins.pd.PDParams;
import org.cobweb.cobweb2.plugins.production.ProductionParams;
import org.cobweb.cobweb2.plugins.waste.WasteParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfSaveInstanceClass;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

/**
 * Used to organize, modify, and access simulation parameters.
 */
public class SimulationConfig implements SimulationParams, ParameterSerializable {

	public String fileName = "default simulation";

	/**
	 * Random number generator seed for repeating the simulation exactly.
	 */
	@ConfDisplayName("Random seed")
	@ConfXMLTag("randomSeed")
	public long randomSeed = 42;

	/**
	 * Number of Agent types.
	 */
	@ConfDisplayName("Agent types")
	@ConfXMLTag("AgentTypeCount")
	public void setAgentTypes(int count) {
		this.agentTypeCount = count;
		agentCountChanged();
	}

	@Override
	public int getAgentTypes() {
		return agentTypeCount;
	}

	private int agentTypeCount = 4;

	/**
	 * Class name of the controller object.
	 */
	@ConfDisplayName("Controller type")
	@ConfXMLTag("ControllerName")
	public void setControllerName(String name) {
		controllerName = name;
		try {
			controllerParams = (ControllerParams) Class.forName(controllerName + "Params")
					.getConstructor(SimulationParams.class)
					.newInstance((SimulationParams) this);
		} catch (InstantiationError | ClassNotFoundException | NoSuchMethodException |
				InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			throw new RuntimeException("Could not set up controller", ex);
		}
	}

	private String controllerName = GeneticController.class.getName();

	public String getControllerName() {
		return controllerName;
	}

	/**
	 * Class name of the agent object
	 */
	@ConfDisplayName("Agent type")
	@ConfXMLTag("AgentName")
	public String agentName = ComplexAgent.class.getName();

	/**
	 * Class name of environment object
	 */
	@ConfDisplayName("Environment type")
	@ConfXMLTag("EnvironmentName")
	public String environmentName = ComplexEnvironment.class.getName();

	/**
	 * Keeps the existing food on the grid.
	 */
	@ConfDisplayName("Keep old array")
	@ConfXMLTag("keepOldArray")
	public boolean keepOldArray = false;

	/**
	 * Keeps existing waste on the grid.
	 */
	@ConfDisplayName("Keep old waste")
	@ConfXMLTag("keepOldDrops")
	public boolean keepOldDrops = false;

	/**
	 * Keeps existing agents.
	 */
	@ConfDisplayName("Keep old agents")
	@ConfXMLTag("keepOldAgents")
	public boolean keepOldAgents = false;

	/**
	 * Spawns new agents.
	 */
	@ConfDisplayName("Spawn new agents")
	@ConfXMLTag("spawnNewAgents")
	public boolean spawnNewAgents = true;

	/**
	 * Keeps old communication packets.
	 */
	@ConfDisplayName("Keep old packets")
	@ConfXMLTag("keepOldPackets")
	public boolean keepOldPackets = false;


	@ConfXMLTag("Environment")
	public ComplexEnvironmentParams envParams = new ComplexEnvironmentParams();

	@ConfXMLTag("Agents")
	public AgentParams agentParams = new AgentParams(this);

	@ConfXMLTag("FoodGrowth")
	public FoodGrowthParams foodParams = new FoodGrowthParams(this);

	@ConfXMLTag("PrisonersDilemma")
	public PDParams pdParams = new PDParams(this);

	@ConfXMLTag("Waste")
	public WasteParams wasteParams = new WasteParams(this);

	@ConfXMLTag("Production")
	public ProductionParams prodParams = new ProductionParams(this);

	@ConfXMLTag("Abiotic")
	public AbioticParams abioticParams = new AbioticParams(this);

	@ConfXMLTag("Learning")
	public LearningParams learningParams = new LearningParams(this);

	@ConfXMLTag("Disease")
	public DiseaseParams diseaseParams = new DiseaseParams(this);

	@ConfXMLTag("ga")
	public GeneticParams geneticParams = new GeneticParams(this);

	@ConfSaveInstanceClass
	@ConfXMLTag("ControllerConfig")
	public ControllerParams controllerParams = new GeneticControllerParams(this);

	/**
	 * Creates the default Cobweb simulation parameters.
	 */
	public SimulationConfig() {
	}


	private void agentCountChanged() {
		for (Field f : this.getClass().getFields()) {
			if (ResizableParam.class.isAssignableFrom(f.getType())) {
				ResizableParam param;
				try {
					param = (ResizableParam) f.get(this);
					param.resize(this);
				} catch (IllegalAccessException ex) {
					throw new RuntimeException("Something broke in agentCountChanged()", ex);
				}
			}
		}
	}

	public boolean isContinuation() {
		return
				keepOldAgents ||
				keepOldArray ||
				keepOldPackets ||
				keepOldDrops;
	}

	@Override
	public List<String> getPluginParameters() {
		List<String> result = new ArrayList<String>();
		result.addAll(this.prodParams.getStatePluginKeys());
		result.addAll(this.abioticParams.getStatePluginKeys());

		return result;
	}

	private static final long serialVersionUID = 2L;

	public <T> T getParam(Class<T> pt) {
		if (pt.isAssignableFrom(this.getClass())) {
			@SuppressWarnings("unchecked")
			T result = (T) this;
			return result;
		} else {
			for (Field f : this.getClass().getFields()) {
				if (pt.isAssignableFrom(f.getType())) {
					try {
						@SuppressWarnings("unchecked")
						T result = (T) f.get(this);
						return result;
					} catch (IllegalAccessException ex) {
						throw new RuntimeException("Could not get parameter " + f.getName(), ex);
					}
				}
			}
		}
		return null;
	}
}
