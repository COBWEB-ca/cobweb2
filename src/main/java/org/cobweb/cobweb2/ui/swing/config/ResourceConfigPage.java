package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.food.ComplexFoodParams;
import org.cobweb.cobweb2.plugins.food.FoodGrowthParams;
import org.cobweb.swingutil.ColorLookup;


public class ResourceConfigPage extends TwoTableConfigPage<FoodGrowthParams, ComplexFoodParams>{

	public ResourceConfigPage(FoodGrowthParams params, ColorLookup agentColors) {
		super(FoodGrowthParams.class, params, "Resource Parameters", agentColors, "Value", null, "Resource Type Parameters", "Food");
		setMainPanelHeight(100);
	}
}
