/**
 *
 */
package cwcore;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;

/**
 * Parameters for GeneticController
 */
public class GeneticControllerParams extends AbstractReflectionParams {

	private static final long serialVersionUID = -1252142643022378114L;

	/**
	 * Random seed used to initialize the behaviour array. 
	 */
	@ConfDisplayName("Array initialization random seed")
	@ConfXMLTag("RandomSeed")
	public long randomSeed = 42;

}
