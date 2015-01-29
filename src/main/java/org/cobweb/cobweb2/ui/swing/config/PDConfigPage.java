package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.pd.PDParams;



public class PDConfigPage extends TableConfigPage<PDParams> {
	public PDConfigPage(PDParams params) {
		super(new PDParams[] { params}, "Prisoner's Dilemma Parameters", "Value");
	}
}
