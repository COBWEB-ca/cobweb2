package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.impl.learning.LearningAgentParams;
import org.cobweb.swingutil.ColorLookup;


public class LearningConfigPage extends TableConfigPage<LearningAgentParams> {

	public LearningConfigPage(LearningAgentParams[] params, ColorLookup agentColors) {
		super(params, "Agent Learning Parameters", agentColors);
	}
}
