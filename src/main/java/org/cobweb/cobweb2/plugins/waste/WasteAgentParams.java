package org.cobweb.cobweb2.plugins.waste;

import java.util.Arrays;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.ResizableParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.CloneHelper;
import org.cobweb.util.MutatableFloat;
import org.cobweb.util.MutatableInt;

public class WasteAgentParams implements ResizableParam {

	/**
	 * Enable waste creation.
	 */
	@ConfXMLTag("wasteMode")
	@ConfDisplayName("Produce Waste")
	public boolean wasteMode = false;

	/**
	 * Can agent consume waste?
	 */
	@ConfXMLTag("canConsume")
	@ConfDisplayName("Consume waste")
	@ConfList(indexName = "waste", startAtOne = true)
	public boolean[] canConsume = new boolean[0];

	/**
	 * Energy gained (lost when negative) when consuming waste.
	 */
	@ConfXMLTag("wasteConsumptionEnergy")
	@ConfDisplayName("Waste consumption energy")
	public MutatableInt consumeEnergy = new MutatableInt(0);

	/**
	 * Energy lost when stepping into waste.
	 * Has no effect when agent can consume waste.
	 */
	@ConfXMLTag("wastePen")
	@ConfDisplayName("Step waste energy loss")
	public int wastePen = 2;

	/**
	 * Waste is produced when this amount of energy is gained.
	 */
	@ConfXMLTag("wasteGain")
	@ConfDisplayName("Waste gain limit")
	public MutatableInt wasteLimitGain = new MutatableInt(100);

	/**
	 * Waste is produced when this amount of energy is lost.
	 */
	@ConfXMLTag("wasteLoss")
	@ConfDisplayName("Waste loss limit")
	public MutatableInt wasteLimitLoss = new MutatableInt(0);

	/**
	 * Waste decay rate.
	 * Formula for decay is: amount = wasteInit * e ^ -rate * time
	 */
	@ConfXMLTag("wasteRate")
	@ConfDisplayName("Waste decay")
	public MutatableFloat wasteDecay = new MutatableFloat(0.5f);
	/**
	 * Initial waste amount.
	 */
	@ConfXMLTag("wasteInit")
	@ConfDisplayName("Waste initial amount")
	public MutatableInt  wasteInit = new MutatableInt(100);

	public WasteAgentParams(AgentFoodCountable envParams) {
		resize(envParams);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		canConsume  = Arrays.copyOf(canConsume, envParams.getAgentTypes());
	}

	@Override
	protected WasteAgentParams clone() {
		try {
			WasteAgentParams copy = (WasteAgentParams) super.clone();
			copy.canConsume = Arrays.copyOf(this.canConsume, this.canConsume.length);

			CloneHelper.resetMutatable(copy);
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}