package org.cobweb.cobweb2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexAgentParams;
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

/**
 * Used to organize, modify, and access simulation parameters.
 */
public class SimulationConfig implements SimulationParams {
	public String fileName = null;

	public ComplexEnvironmentParams envParams;

	public ComplexAgentParams[] agentParams;

	public FoodGrowthParams foodParams;

	public WasteParams wasteParams;

	public ProductionParams prodParams;

	public TemperatureParams tempParams;

	public LearningParams learningParams;

	public DiseaseParams diseaseParams;

	public GeneticParams geneticParams;

	public ControllerParams controllerParams;

	/**
	 * Creates the default Cobweb simulation parameters.
	 */
	public SimulationConfig() {
		envParams = new ComplexEnvironmentParams();
		setDefaultClassReferences();

		agentParams = new ComplexAgentParams[envParams.getAgentTypes()];
		for (int i = 0; i < envParams.getAgentTypes(); i++) {
			agentParams[i] = new ComplexAgentParams(envParams);
		}

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

		{
			ComplexAgentParams[] n = Arrays.copyOf(this.agentParams, count);
			for (int i = 0; i < this.agentParams.length && i < count; i++) {
				n[i].resize(envParams);
			}
			for (int i = this.agentParams.length; i < count; i++) {
				n[i] = new ComplexAgentParams(envParams);
			}
			this.agentParams = n;
		}

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

}
