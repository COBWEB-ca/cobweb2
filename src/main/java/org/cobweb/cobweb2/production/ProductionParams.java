package org.cobweb.cobweb2.production;

import java.util.Arrays;
import java.util.List;

import org.cobweb.cobweb2.core.StatePluginSource;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;


public class ProductionParams implements ParameterSerializable, StatePluginSource {
	/**
	 *
	 */
	private static final long serialVersionUID = 7452975610085145539L;

	static final String STATE_NAME_PRODHUNT = "Production Value";

	@Override
	public List<String> getStatePluginKeys() {
		return Arrays.asList(STATE_NAME_PRODHUNT);
	}

	/**
	 * Agent type index.
	 */
	@ConfXMLTag("Index")
	public int type = -1;

	/**
	 * Enable production
	 */
	@ConfDisplayName("Production")
	@ConfXMLTag("productionMode")
	public boolean productionMode;


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
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}
}
