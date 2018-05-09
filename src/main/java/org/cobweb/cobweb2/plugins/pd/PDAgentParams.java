package org.cobweb.cobweb2.plugins.pd;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;
import org.cobweb.util.CloneHelper;
import org.cobweb.util.MutatableFloat;
import org.cobweb.util.MutatableInt;


public class PDAgentParams implements ParameterSerializable {

	@ConfDisplayName("PD Enabled")
	@ConfXMLTag("pdEnabled")
	public boolean pdEnabled = true;

	/**
	 * Use tit-for-tat strategy for prisoner's dilemma.
	 */
	@ConfDisplayName("PD:Use Tit-for-tat")
	@ConfXMLTag("pdTitForTat")
	public boolean pdTitForTat = false;

	/**
	 * Percentage of agents that will be cooperators initially, the rest are cheaters.
	 */
	@ConfDisplayName("PD Cooperation probability")
	@ConfXMLTag("pdCoopProb")
	public MutatableInt pdCoopProb = new MutatableInt(50);

	@ConfDisplayName("PD similarity preference")
	@ConfXMLTag("pdSimilaritySlope")
	public MutatableFloat pdSimilaritySlope = new MutatableFloat(0.0f);

	@ConfDisplayName("PD neutral similarity")
	@ConfXMLTag("pdSimilarityNeutral")
	public MutatableFloat pdSimilarityNeutral = new MutatableFloat(0.9f);


	@Override
	protected PDAgentParams clone() {
		try {
			PDAgentParams copy = (PDAgentParams)super.clone();
			CloneHelper.resetMutatable(copy);
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
