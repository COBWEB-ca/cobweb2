package org.cobweb.cobweb2;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.cobweb.cobweb2.plugins.PerTypeParam;
import org.cobweb.cobweb2.plugins.abiotic.AbioticParams;
import org.cobweb.cobweb2.plugins.disease.DiseaseParams;
import org.cobweb.cobweb2.plugins.food.FoodGrowthParams;
import org.cobweb.cobweb2.plugins.genetics.GeneticParams;
import org.cobweb.cobweb2.plugins.pd.PDParams;
import org.cobweb.cobweb2.plugins.production.ProductionParams;
import org.cobweb.cobweb2.plugins.waste.WasteParams;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

/**
 * Used to organize, modify, and access simulation parameters.
 */
public class SimulationConfig implements SimulationParams, ParameterSerializable {

	public String fileName = "default simulation";

	// Loaded manually first because others depend on it
	public ComplexEnvironmentParams envParams = new ComplexEnvironmentParams();

	@ConfXMLTag("Agents")
	public AgentParams agentParams = new AgentParams(envParams);

	@ConfXMLTag("FoodGrowth")
	public FoodGrowthParams foodParams = new FoodGrowthParams(envParams);

	@ConfXMLTag("PrisonersDilemma")
	public PDParams pdParams = new PDParams(envParams);

	@ConfXMLTag("Waste")
	public WasteParams wasteParams = new WasteParams(envParams);

	@ConfXMLTag("Production")
	public ProductionParams prodParams = new ProductionParams(envParams);

	// FIXME!!! upgrade config xml!
	@ConfXMLTag("Abiotic")
	public AbioticParams abioticParams = new AbioticParams(envParams);

	@ConfXMLTag("Learning")
	public LearningParams learningParams = new LearningParams(envParams);

	@ConfXMLTag("Disease")
	public DiseaseParams diseaseParams = new DiseaseParams(envParams);

	@ConfXMLTag("ga")
	public GeneticParams geneticParams = new GeneticParams(envParams);

	@ConfXMLTag("ControllerConfig")
	public ControllerParams controllerParams;

	/**
	 * Creates the default Cobweb simulation parameters.
	 */
	public SimulationConfig() {
		setDefaultClassReferences();
	}

	protected void setDefaultClassReferences() {
		// These are not set in ComplexEnvironmentParams to avoid dependencies

		envParams.controllerName = GeneticController.class.getName();
		controllerParams = new GeneticControllerParams(this);

		envParams.agentName = ComplexAgent.class.getName();

		envParams.environmentName = ComplexEnvironment.class.getName();
	}

	public void SetAgentTypeCount(int count) {
		this.envParams.agentTypeCount = count;

		// NOTE: update this when adding new params
		List<? extends PerTypeParam> typeCountDependents = Arrays.asList(
				agentParams,
				foodParams,
				pdParams,
				wasteParams,
				prodParams,
				abioticParams,
				learningParams,
				diseaseParams,
				geneticParams,
				controllerParams
				);

		for (PerTypeParam param : typeCountDependents) {
			param.resize(envParams);
		}
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
		result.addAll(this.abioticParams.getStatePluginKeys());

		return result;
	}

	@Override
	public AgentFoodCountable getCounts() {
		return this.envParams;
	}

	private static final long serialVersionUID = 2L;
}
