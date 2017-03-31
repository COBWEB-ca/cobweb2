package org.cobweb.cobweb2.plugins.swarm;

import java.util.Arrays;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.ResizableParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;


public class SwarmAgentParams implements ResizableParam {

	/**
	 * Effect of nearby agents of specific types on this agent
	 */
	@ConfDisplayName("Swarm Agent")
	@ConfXMLTag("Effects")
	@ConfList(indexName = "Agent", startAtOne = true)
	public PairwiseEffect[] effects = new PairwiseEffect[0];

	@Deprecated // for reflection use only!
	public SwarmAgentParams() {
	}

	public SwarmAgentParams(AgentFoodCountable size) {
		resize(size);
	}

	@Override
	public void resize(AgentFoodCountable size) {
		int prevSize = effects.length;
		PairwiseEffect[] n = Arrays.copyOf(effects, size.getAgentTypes());
		for (int i = prevSize; i < size.getAgentTypes(); i++) {
			n[i] = new PairwiseEffect();
		}
		this.effects = n;
	}

	@Override
	public SwarmAgentParams clone(){
		try {
			SwarmAgentParams copy = (SwarmAgentParams) super.clone();
			copy.effects = new PairwiseEffect[this.effects.length];

			for (int i = 0; i < copy.effects.length; i++) {
				copy.effects[i] = this.effects[i].clone();
			}
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
