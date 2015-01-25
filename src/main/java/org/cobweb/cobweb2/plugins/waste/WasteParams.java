package org.cobweb.cobweb2.plugins.waste;

import java.util.Arrays;

import org.cobweb.cobweb2.impl.AgentFoodCountable;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;


public class WasteParams implements ParameterSerializable {

	@ConfXMLTag("AgentParams")
	@ConfList(indexName = "Agent", startAtOne = true)
	public WasteAgentParams[] agentParams = new WasteAgentParams[0];

	public void resize(AgentFoodCountable envParams) {
		WasteAgentParams[] n = Arrays.copyOf(agentParams, envParams.getAgentTypes());

		for (int i = agentParams.length; i < envParams.getAgentTypes(); i++) {
			n[i] = new WasteAgentParams();
		}
		agentParams = n;
	}

	private static final long serialVersionUID = 1L;
}
