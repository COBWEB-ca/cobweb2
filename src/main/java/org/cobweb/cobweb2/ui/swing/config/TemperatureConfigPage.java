package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.abiotic.TemperatureAgentParams;
import org.cobweb.cobweb2.plugins.abiotic.TemperatureParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;

public class TemperatureConfigPage extends TwoTableConfigPage<TemperatureParams, TemperatureAgentParams> {

	public TemperatureConfigPage(TemperatureParams params, ChoiceCatalog phenotypeCatalog, ColorLookup colors) {
		super(TemperatureParams.class, params, "Environment Bands", colors, "Abiotic Factor", phenotypeCatalog);
	}

}
