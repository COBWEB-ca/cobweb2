package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

public class AgentFactorParams implements ParameterSerializable {

	/**
	 * Preferred abiotic factor value of the agent type.
	 */
	@ConfDisplayName("Preferred value")
	@ConfXMLTag("PreferedValue")
	public float preferedValue;


	/**
	 * Range of value from preferred that can be tolerated.
	 */
	@ConfDisplayName("Preferred value range")
	@ConfXMLTag("PreferedRange")
	public float preferedRange;


	/**
	 * How much of an effect deviation from the preferred value range will have.
	 */
	@ConfDisplayName("Difference factor")
	@ConfXMLTag("DifferenceFactor")
	public float differenceFactor;


	@ConfDisplayName("Parameter")
	@ConfXMLTag("Parameter")
	public Phenotype parameter = new NullPhenotype();

	private static final long serialVersionUID = 1L;
}