package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.toxin.ToxinAgentParams;
import org.cobweb.cobweb2.plugins.toxin.ToxinParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;


public class ToxinConfigPage extends TableConfigPage<ToxinAgentParams> {

	public ToxinConfigPage(ToxinParams params, ChoiceCatalog catalog, ColorLookup agentColors) {
		super(params.agentParams, "Toxin Parameters", agentColors, catalog);
	}
}
