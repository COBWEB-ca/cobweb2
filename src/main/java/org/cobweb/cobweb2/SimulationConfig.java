package org.cobweb.cobweb2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cobweb.cobweb2.impl.AgentFoodCountable;
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
import org.cobweb.cobweb2.plugins.food.ComplexFoodParams;
import org.cobweb.cobweb2.plugins.genetics.GeneticParams;
import org.cobweb.cobweb2.plugins.production.ProductionParams;
import org.cobweb.cobweb2.plugins.waste.WasteParams;

/**
 * Used to organize, modify, and access simulation parameters.
 */
public class SimulationConfig implements SimulationParams {
	String fileName = null;

	public ComplexEnvironmentParams envParams;

	public GeneticParams geneticParams;

	public ComplexAgentParams[] agentParams;

	public ProductionParams prodParams;

	public LearningParams learningParams;

	public ComplexFoodParams[] foodParams;

	public DiseaseParams diseaseParams;

	public TemperatureParams tempParams;

	public WasteParams wasteParams;

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
			agentParams[i].type = i;
		}

		foodParams = new ComplexFoodParams[envParams.getFoodTypes()];
		for (int i = 0; i < envParams.getFoodTypes(); i++) {
			foodParams[i] = new ComplexFoodParams();
			foodParams[i].type = i;
		}

		geneticParams = new GeneticParams(envParams);

		diseaseParams = new DiseaseParams(envParams);

		prodParams = new ProductionParams(envParams);

		wasteParams = new WasteParams(envParams);

		tempParams = new TemperatureParams(envParams);

		learningParams = new LearningParams(envParams);

		controllerParams = new GeneticControllerParams(this);

		fileName = "default simulation";
	}

	protected void setDefaultClassReferences() {
		envParams.controllerName = GeneticController.class.getName();
		envParams.agentName = ComplexAgent.class.getName();
		envParams.environmentName = ComplexEnvironment.class.getName();
	}

	/**
	 * @return Agent parameters
	 */
	public ComplexAgentParams[] getAgentParams() {
		return agentParams;
	}

	public ProductionParams getProdParams() {
		return prodParams;
	}


	/**
	 * @return Disease parameters
	 */
	public DiseaseParams getDiseaseParams() {
		return diseaseParams;
	}

	/**
	 * @return Environment parameters
	 */
	public ComplexEnvironmentParams getEnvParams() {
		return envParams;
	}

	/**
	 * @return Simulation configuration file name
	 */
	public String getFilename() {
		return fileName;
	}

	/**
	 * @return Food parameters
	 */
	public ComplexFoodParams[] getFoodParams() {
		return foodParams;
	}

	/**
	 * @return Genetic parameters
	 */
	public GeneticParams getGeneticParams() {
		return geneticParams;
	}

	/**
	 * @return Temperature parameters
	 */
	public TemperatureParams getTempParams() {
		return tempParams;
	}

	public LearningParams getLearningParams() {
		return learningParams;
	}


	public void SetAgentTypeCount(int count) {
		this.envParams.agentTypeCount = count;
		this.envParams.foodTypeCount = count;

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

		{
			ComplexFoodParams[] n = Arrays.copyOf(this.foodParams, count);
			for (int i = this.foodParams.length; i < count; i++) {
				n[i] = new ComplexFoodParams();
			}
			this.foodParams = n;
		}

		this.diseaseParams.resize(envParams);
		this.prodParams.resize(envParams);
		this.geneticParams.resize(envParams);
		this.tempParams.resize(envParams);
		this.wasteParams.resize(envParams);
		this.learningParams.resize(envParams);
		this.controllerParams.resize(envParams);

	}

	public ControllerParams getControllerParams() {
		return controllerParams;
	}

	public void setControllerParams(ControllerParams params) {
		controllerParams = params;
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
		if (this.prodParams != null)
			result.addAll(this.prodParams.getStatePluginKeys());
		result.addAll(this.tempParams.getStatePluginKeys());

		return result;
	}

	@Override
	public AgentFoodCountable getCounts() {
		return this.envParams;
	}

	public WasteParams getWasteParams() {
		return this.wasteParams;
	}

}
