package org.cobweb.cobweb2.plugins.production;

import java.util.Arrays;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.cobweb2.plugins.TempEffectParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.CloneHelper;
import org.cobweb.util.MutatableInt;


public class ProductionAgentParams implements AgentState {

	/**
	 * Enable production
	 */
	@ConfDisplayName("Production")
	@ConfXMLTag("productionMode")
	public boolean productionMode;


	@ConfXMLTag("productionCost")
	@ConfDisplayName("Production Cost")
	public MutatableInt productionCost = new MutatableInt(0);

	@ConfXMLTag("productPrice")
	@ConfDisplayName("Product selling price")
	public MutatableInt productPrice = new MutatableInt(0);

	@ConfXMLTag("maxUnsold")
	@ConfDisplayName("Max unsold products")
	public MutatableInt maxUnsold = new MutatableInt(100);

	@ConfXMLTag("productExpiry")
	@ConfDisplayName("Product expiry period")
	public MutatableInt productExpiry = new MutatableInt(1000);

	@ConfXMLTag("productEffect")
	@ConfDisplayName("Product effect")
	public TempEffectParam productEffect = new TempEffectParam();

	@ConfXMLTag("InitProdChance")
	@ConfDisplayName("Initial production percentage roll")
	public float initProdChance = 0.8f;

	@ConfXMLTag("LowDemThresh")
	@ConfDisplayName("The maximum prodVal to consider \"low\"")
	public float lowDemandThreshold = 5f;

	@ConfXMLTag("LowDemProdChance")
	@ConfDisplayName("Percentage roll to produce in low demand area")
	public float lowDemandProdChance = 0.001f;

	@ConfXMLTag("SweetDemThresh")
	@ConfDisplayName("The maximum prodVal to consider \"sweet\"")
	public float sweetDemandThreshold = 6f;

	@ConfXMLTag("SweetDemStartChance")
	@ConfDisplayName("Minimum roll percentage in sweet zone")
	public float sweetDemandStartChance = 0.001f;

	@ConfXMLTag("HiDemCutoff")
	@ConfDisplayName("Maximal prodVal to produce on")
	public float highDemandCutoff = 20f;

	@ConfXMLTag("HiDemProdChance")
	@ConfDisplayName("Maximum roll percentage in high zone")
	public float highDemandProdChance = 0.001f;

	@ConfXMLTag("agentConsumptionFactor")
	@ConfDisplayName("Consumption factor from Agent")
	@ConfList(indexName = "Agent", startAtOne = true)
	public float[] agentConsumptionFactor = new float[0]; // default zero

	@ConfXMLTag("agentMaxConsFactor")
	@ConfDisplayName("Maximum consumption factor from Agent")
	@ConfList(indexName = "Agent", startAtOne = true)
	public float[] agentMaxConsFactor = new float[0]; // default zero

	public ProductionAgentParams(AgentFoodCountable size) {
		resize(size);
	}

	public void resize(AgentFoodCountable envParams) {
		agentConsumptionFactor = Arrays.copyOf(agentConsumptionFactor, envParams.getAgentTypes());
		agentMaxConsFactor = Arrays.copyOf(agentMaxConsFactor, envParams.getAgentTypes());
		Arrays.fill(agentMaxConsFactor, 1);
	}

	@Override
	public ProductionAgentParams clone() {
		try {
			ProductionAgentParams copy = (ProductionAgentParams) super.clone();
			copy.productEffect = this.productEffect.clone();
			CloneHelper.resetMutatable(copy);
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 2L;
}
