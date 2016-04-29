package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.learning.LearningAgentParams;
import org.cobweb.cobweb2.plugins.learning.LearningParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;


public class LearningConfigPage extends TableConfigPage<LearningAgentParams> {

	public LearningConfigPage(LearningParams params, ChoiceCatalog catalog, ColorLookup agentColors) {
		super(params.agentParams, "Learning Parameters", agentColors, catalog);
	}
}
