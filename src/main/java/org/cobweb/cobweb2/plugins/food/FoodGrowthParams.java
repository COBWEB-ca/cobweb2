package org.cobweb.cobweb2.plugins.food;

import java.util.Arrays;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerTypeParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;


public class FoodGrowthParams implements ParameterSerializable, PerTypeParam {
	/**
	 * Probability that food will grow around similar existing food.
	 */
	@ConfDisplayName("Like food probability")
	@ConfXMLTag("likeFoodProb")
	public float likeFoodProb = 0;

	@ConfXMLTag("FoodParams")
	@ConfList(indexName = "Food", startAtOne = true)
	public ComplexFoodParams[] foodParams = new ComplexFoodParams[0];

	public FoodGrowthParams(AgentFoodCountable initialSize) {
		resize(initialSize);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		ComplexFoodParams[] n = Arrays.copyOf(foodParams, envParams.getAgentTypes());

		for (int i = foodParams.length; i < envParams.getAgentTypes(); i++) {
			n[i] = new ComplexFoodParams();
		}
		foodParams = n;
	}

	private static final long serialVersionUID = 1L;
}
