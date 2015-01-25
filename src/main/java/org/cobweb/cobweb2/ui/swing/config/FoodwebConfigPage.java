package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.impl.FoodwebParams;
import org.cobweb.swingutil.ColorLookup;


public class FoodwebConfigPage extends TableConfigPage<FoodwebParams> {

	public FoodwebConfigPage(FoodwebParams[] params, ColorLookup agentColors) {
		super(params, "Food Parameters", agentColors);
	}
}
