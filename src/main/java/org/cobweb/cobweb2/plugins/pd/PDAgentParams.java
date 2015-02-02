package org.cobweb.cobweb2.plugins.pd;

import org.cobweb.cobweb2.core.Mutatable;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;


public class PDAgentParams implements ParameterSerializable {

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
	@Mutatable
	public int pdCoopProb = 50;

	@ConfDisplayName("PD similarity preference")
	@ConfXMLTag("pdSimilaritySlope")
	@Mutatable
	public float pdSimilaritySlope = 0.0f;

	@ConfDisplayName("PD neutral similarity")
	@ConfXMLTag("pdSimilarityNeutral")
	@Mutatable
	public float pdSimilarityNeutral = 0.9f;


	@Override
	protected PDAgentParams clone() {
		try {
			return (PDAgentParams)super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
