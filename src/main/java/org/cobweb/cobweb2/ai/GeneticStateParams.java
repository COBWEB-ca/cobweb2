package org.cobweb.cobweb2.ai;

import java.util.Arrays;

import org.cobweb.cobweb2.core.params.SimulationParams;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;


public class GeneticStateParams implements ParameterSerializable {


	private static final long serialVersionUID = -7136336946033726870L;

	@ConfXMLTag("AgentParams")
	@ConfList(indexName = "Agent", startAtOne = true)
	public GeneticStateAgentParams[] agentParams = new GeneticStateAgentParams[0];

	private SimulationParams simParam;

	GeneticStateParams(SimulationParams simParam) {
		this.simParam = simParam;
		int typeCount = simParam.getCounts().getAgentTypes();
		resize(typeCount);
	}

	public void resize(int agentTypes) {
		GeneticStateAgentParams[] n = Arrays.copyOf(agentParams, agentTypes);

		for (int i = agentParams.length; i < agentTypes; i++) {
			n[i] = new GeneticStateAgentParams(simParam);
		}
		agentParams = n;
	}

}
