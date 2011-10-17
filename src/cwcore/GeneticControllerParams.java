/**
 *
 */
package cwcore;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;
import cwcore.complexParams.AgentFoodCountable;
import cwcore.complexParams.ControllerParams;

/**
 * Parameters for GeneticController
 */
public class GeneticControllerParams extends AbstractReflectionParams implements ControllerParams {

	private static final long serialVersionUID = -1252142643022378114L;

	/**
	 * Random seed used to initialize the behaviour array. 
	 */
	@ConfDisplayName("Array initialization random seed")
	@ConfXMLTag("RandomSeed")
	public long randomSeed = 42;

	@ConfDisplayName("Parameter Plugins")
	@ConfXMLTag("PluginParams")
	public GeneticStateParams agentParams = new GeneticStateParams();

	@Override
	public void setTypeCount(int count) {
		agentParams.setTypeCount(count);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		agentParams.resize(envParams.getAgentTypes());
	}
}
