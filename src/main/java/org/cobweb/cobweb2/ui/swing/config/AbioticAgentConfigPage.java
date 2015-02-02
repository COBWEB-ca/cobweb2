package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.abiotic.AbioticAgentParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;

public class AbioticAgentConfigPage extends TableConfigPage<AbioticAgentParams> {

	public AbioticAgentConfigPage(AbioticAgentParams[] params, ChoiceCatalog phenotypeCatalog, ColorLookup colors) {
		super(params, "Abiotic Factor", colors, phenotypeCatalog);
	}

}
