/**
 *
 */
package cwcore.complexParams;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.CobwebParam;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;


public class ComplexFoodParams extends AbstractReflectionParams implements CobwebParam {
	/**
	 *
	 */
	private static final long serialVersionUID = 4935757387466603476L;

	@ConfXMLTag("Index")
	public int type;

	@ConfDisplayName("Initial amount")
	@ConfXMLTag("Food")
	public int initial;

	@ConfDisplayName("Spawn rate")
	@ConfXMLTag("FoodRate")
	public float dropRate;

	@ConfDisplayName("Growth rate")
	@ConfXMLTag("FoodGrow")
	public int growRate;

	@ConfDisplayName("Depletion rate")
	@ConfXMLTag("FoodDeplete")
	public float depleteRate;

	@ConfDisplayName("Depletion time")
	@ConfXMLTag("DepleteTimeSteps")
	public int depleteTime;

	@ConfDisplayName("Draught period")
	@ConfXMLTag("DraughtPeriod")
	public int draughtPeriod;

	public ComplexFoodParams() {
		initial = 20;
		dropRate = 0.0f;
		growRate = 4;
		depleteRate = 0.9f;
		depleteTime = 40;
		draughtPeriod = 0;

		type = -1;
	}
}