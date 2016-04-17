package org.cobweb.cobweb2.plugins.production;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfDisplayName;
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

	@Override
	public ProductionAgentParams clone() {
		try {
			ProductionAgentParams copy = (ProductionAgentParams) super.clone();
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
