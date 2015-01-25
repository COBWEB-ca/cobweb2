package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.food.ComplexFoodParams;
import org.cobweb.swingutil.ColorLookup;


public class ResourceConfigPage extends TableConfigPage<ComplexFoodParams>{

	public ResourceConfigPage(ComplexFoodParams[] params, ColorLookup agentColors) {
		super(params, "Resource Parameters", agentColors, "Food");
	}
}
