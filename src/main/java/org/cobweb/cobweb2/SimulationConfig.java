package org.cobweb.cobweb2;

import java.util.ArrayList;
import java.util.List;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.impl.AgentParams;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.cobweb2.impl.ComplexEnvironmentParams;
import org.cobweb.cobweb2.impl.ControllerParams;
import org.cobweb.cobweb2.impl.SimulationParams;
import org.cobweb.cobweb2.impl.ai.GeneticController;
import org.cobweb.cobweb2.impl.ai.GeneticControllerParams;
import org.cobweb.cobweb2.impl.learning.LearningParams;
import org.cobweb.cobweb2.plugins.abiotic.TemperatureParams;
import org.cobweb.cobweb2.plugins.disease.DiseaseParams;
import org.cobweb.cobweb2.plugins.food.FoodGrowthParams;
import org.cobweb.cobweb2.plugins.genetics.GeneticParams;
import org.cobweb.cobweb2.plugins.production.ProductionParams;
import org.cobweb.cobweb2.plugins.waste.WasteParams;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

/**
 * Used to organize, modify, and access simulation parameters.
 */
public class SimulationConfig implements SimulationParams, ParameterSerializable {

	public String fileName = null;

	// Loaded manually first because others depend on it
	public ComplexEnvironmentParams envParams;

	@ConfXMLTag("Agents")
	public AgentParams agentParams;

	@ConfXMLTag("FoodGrowth")
	public FoodGrowthParams foodParams;

	@ConfXMLTag("Waste")
	public WasteParams wasteParams;

	@ConfXMLTag("Production")
	public ProductionParams prodParams;

	@ConfXMLTag("Temperature")
	public TemperatureParams tempParams;

	// Loaded manually because it's optional
	public LearningParams learningParams;

	@ConfXMLTag("Disease")
	public DiseaseParams diseaseParams;

	@ConfXMLTag("ga")
	public GeneticParams geneticParams;

	// Loaded manually because it can be different things
	public ControllerParams controllerParams;

	/**
	 * Creates the default Cobweb simulation parameters.
	 */
	public SimulationConfig() {
		envParams = new ComplexEnvironmentParams();
		setDefaultClassReferences();

		agentParams = new AgentParams(envParams);

		foodParams = new FoodGrowthParams(envParams);

		wasteParams = new WasteParams(envParams);

		prodParams = new ProductionParams(envParams);

		tempParams = new TemperatureParams(envParams);

		learningParams = new LearningParams(envParams);

		diseaseParams = new DiseaseParams(envParams);

		geneticParams = new GeneticParams(envParams);

		controllerParams = new GeneticControllerParams(this);

		fileName = "default simulation";
	}

	protected void setDefaultClassReferences() {
		envParams.controllerName = GeneticController.class.getName();
		envParams.agentName = ComplexAgent.class.getName();
		envParams.environmentName = ComplexEnvironment.class.getName();
	}

	public void SetAgentTypeCount(int count) {
		this.envParams.agentTypeCount = count;

		this.agentParams.resize(envParams);
		this.foodParams.resize(envParams);
		this.wasteParams.resize(envParams);
		this.prodParams.resize(envParams);
		this.tempParams.resize(envParams);
		this.learningParams.resize(envParams);
		this.diseaseParams.resize(envParams);
		this.geneticParams.resize(envParams);
		this.controllerParams.resize(envParams);
	}

	public boolean isContinuation() {
		return
				envParams.keepOldAgents ||
				envParams.keepOldArray ||
				envParams.keepOldPackets ||
				envParams.keepOldWaste;
	}

	@Override
	public List<String> getPluginParameters() {
		List<String> result = new ArrayList<String>();
		result.addAll(this.prodParams.getStatePluginKeys());
		result.addAll(this.tempParams.getStatePluginKeys());

		return result;
	}

	@Override
	public AgentFoodCountable getCounts() {
		return this.envParams;
	}

	private static final long serialVersionUID = 2L;
}
