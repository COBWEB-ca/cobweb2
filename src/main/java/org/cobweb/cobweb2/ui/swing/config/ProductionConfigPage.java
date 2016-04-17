package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.production.ProductionAgentParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;


public class ProductionConfigPage extends TableConfigPage<ProductionAgentParams> {

	public ProductionConfigPage(ProductionAgentParams[] params, ChoiceCatalog choiceCatalog, ColorLookup agentColors) {
		super(params, "Resource Production", agentColors, choiceCatalog);
	}
}
