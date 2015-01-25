package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.production.ProductionParams;
import org.cobweb.swingutil.ColorLookup;


public class ProductionConfigPage extends TableConfigPage<ProductionParams> {

	public ProductionConfigPage(ProductionParams[] params, ColorLookup agentColors) {
		super(params, "Resource Production", agentColors);
	}
}
