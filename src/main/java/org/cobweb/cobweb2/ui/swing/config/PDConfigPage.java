package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.pd.PDAgentParams;
import org.cobweb.cobweb2.plugins.pd.PDParams;
import org.cobweb.swingutil.ColorLookup;



public class PDConfigPage extends TwoTableConfigPage<PDParams, PDAgentParams> {
	public PDConfigPage(PDParams params, ColorLookup colors) {
		super(PDParams.class, params, "Prisoner's Dilemma Parameters", colors);
	}
}
