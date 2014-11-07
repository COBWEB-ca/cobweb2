package temperature;

import ga.GeneticParams.Phenotype;
import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;

/**
 * Contains temperature parameters that are agent type specific.
 */
public class TemperatureAgentParams extends AbstractReflectionParams {

	private static final long serialVersionUID = -832525422408970835L;

	/**
	 * Preferred temperature of the agent type.
	 */
	@ConfXMLTag("PreferedTemp")
	@ConfDisplayName("Preferred value")
	public float preferedTemp;

	/**
	 * Temperature range that can be tolerated from the preferred temperature.
	 */
	@ConfXMLTag("PreferedTempRange")
	@ConfDisplayName("Preferred value range")
	public float preferedTempRange;

	/**
	 * How much of an effect deviation from the preferred temperature range will have. 
	 */
	@ConfXMLTag("DifferenceFactor")
	@ConfDisplayName("Difference factor")
	public float differenceFactor;

	@ConfXMLTag("Parameter")
	@ConfDisplayName("Parameter")
	public Phenotype parameter = new Phenotype();
}
