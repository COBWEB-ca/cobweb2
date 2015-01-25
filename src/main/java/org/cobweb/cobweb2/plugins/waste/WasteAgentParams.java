package org.cobweb.cobweb2.plugins.waste;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

public class WasteAgentParams implements ParameterSerializable {

	/**
	 * Enable waste creation.
	 */
	@ConfXMLTag("wasteMode")
	@ConfDisplayName("Produce Waste")
	public boolean wasteMode = false;

	/**
	 * Energy lost when stepping into waste.
	 */
	@ConfXMLTag("wastePen")
	@ConfDisplayName("Step waste energy loss")
	public int wastePen = 2;

	/**
	 * Waste is produced when this amount of energy is gained.
	 */
	@ConfXMLTag("wasteGain")
	@ConfDisplayName("Waste gain limit")
	public int wasteLimitGain = 100;

	/**
	 * Waste is produced when this amount of energy is lost.
	 */
	@ConfXMLTag("wasteLoss")
	@ConfDisplayName("Waste loss limit")
	public int wasteLimitLoss = 0;

	/**
	 * Waste decay rate.
	 * Formula for decay is: amount = wasteInit * e ^ -rate * time
	 */
	@ConfXMLTag("wasteRate")
	@ConfDisplayName("Waste decay")
	public float wasteDecay = 0.5f;

	/**
	 * Initial waste amount.
	 */
	@ConfXMLTag("wasteInit")
	@ConfDisplayName("Waste initial amount")
	public int wasteInit = 100;

	@Override
	protected WasteAgentParams clone() {
		try {
			return (WasteAgentParams) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}