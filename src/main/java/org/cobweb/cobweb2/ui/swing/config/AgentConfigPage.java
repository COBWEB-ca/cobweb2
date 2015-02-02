package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.impl.ComplexAgentParams;
import org.cobweb.swingutil.ColorLookup;


public class AgentConfigPage extends TableConfigPage<ComplexAgentParams> {

	public AgentConfigPage(ComplexAgentParams[] params, ColorLookup agentColors) {
		super(params, "Agent Parameters", agentColors);
	}
}
