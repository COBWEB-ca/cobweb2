package org.cobweb.cobweb2.ui.swing.config;

import org.cobweb.cobweb2.plugins.swarm.PairwiseEffect;
import org.cobweb.cobweb2.plugins.swarm.SwarmAgentParams;
import org.cobweb.cobweb2.plugins.swarm.SwarmParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;


public class SwarmConfigPage extends TableConfigPage<SwarmAgentParams> {

	private SwarmParams params;


	public SwarmConfigPage(SwarmParams params, ColorLookup agentColors, ChoiceCatalog catalog) {
		super(params.agentParams, "Swarm Parameters", agentColors, catalog);
		this.params = params;
	}


	@Override
	public void validateUI() throws IllegalArgumentException {
		super.validateUI();
		for (SwarmAgentParams param : this.params.agentParams) {
			for (PairwiseEffect effect	: param.effects) {
				effect.validate();
			}
		}
	}

}
