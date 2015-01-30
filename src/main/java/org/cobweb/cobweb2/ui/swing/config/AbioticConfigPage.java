package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.abiotic.AbioticAgentParams;
import org.cobweb.cobweb2.plugins.abiotic.AbioticParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;

public class AbioticConfigPage extends TwoTableConfigPage<AbioticParams, AbioticAgentParams> {

	public AbioticConfigPage(AbioticParams params, ChoiceCatalog phenotypeCatalog, ColorLookup colors) {
		super(AbioticParams.class, params, "Environment Bands", colors, "Abiotic Factor", phenotypeCatalog);
	}

}
