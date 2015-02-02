package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.waste.WasteAgentParams;
import org.cobweb.swingutil.ColorLookup;


public class WasteConfigPage extends TableConfigPage<WasteAgentParams> {

	public WasteConfigPage(WasteAgentParams[] params, ColorLookup agentColors) {
		super(params, "Waste Parameters", agentColors);
	}
}
