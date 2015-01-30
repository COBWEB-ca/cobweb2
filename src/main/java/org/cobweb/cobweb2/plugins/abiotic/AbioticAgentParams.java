package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

/**
 * Contains temperature parameters that are agent type specific.
 */
public class AbioticAgentParams implements ParameterSerializable {

	private static final long serialVersionUID = -832525422408970835L;

	/**
	 * Preferred temperature of the agent type.
	 */
	@ConfXMLTag("PreferedTemp")
	@ConfDisplayName("Preferred value")
	public float preferedValue;

	/**
	 * Temperature range that can be tolerated from the preferred temperature.
	 */
	@ConfXMLTag("PreferedTempRange")
	@ConfDisplayName("Preferred value range")
	public float preferedRange;

	/**
	 * How much of an effect deviation from the preferred temperature range will have.
	 */
	@ConfXMLTag("DifferenceFactor")
	@ConfDisplayName("Difference factor")
	public float differenceFactor;

	@ConfXMLTag("Parameter")
	@ConfDisplayName("Parameter")
	public Phenotype parameter = new NullPhenotype();
}
