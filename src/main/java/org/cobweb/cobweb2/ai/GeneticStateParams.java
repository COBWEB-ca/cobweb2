package org.cobweb.cobweb2.ai;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cobweb.cobweb2.core.params.SimulationParams;
import org.cobweb.cobweb2.io.CobwebParam;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GeneticStateParams implements CobwebParam {


	private static final long serialVersionUID = -7136336946033726870L;

	public GeneticStateAgentParams[] agentParams;

	private int typeCount;

	private static final Pattern agentRE = Pattern.compile("^Agent(\\d+)$");

	private SimulationParams simParam;

	GeneticStateParams(SimulationParams simParam) {
		this.simParam = simParam;
		typeCount = simParam.getCounts().getAgentTypes();
		agentParams = new GeneticStateAgentParams[typeCount];
		init();
	}

	private void init() {
		for (int i = 0; i < agentParams.length; i++) {
			agentParams[i] = new GeneticStateAgentParams(simParam);
		}
	}

	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		agentParams = new GeneticStateAgentParams[typeCount];

		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (!n.getNodeName().equals("AgentParams"))
				continue;

			NodeList nl2 = n.getChildNodes();
			for (int j = 0; j < nl2.getLength(); j++) {
				Node tt = nl2.item(j);
				String name = tt.getNodeName();

				Matcher m = agentRE.matcher(name);
				if (!m.matches())
					continue;

				int id = Integer.parseInt(m.group(1)) - 1;
				if (id >= typeCount)
					continue;

				GeneticStateAgentParams ss = new GeneticStateAgentParams(simParam);
				ss.loadConfig(tt);
				agentParams[id] = ss;
			}
		}
	}

	@Override
	public void saveConfig(Node root, Document document) {
		Node trans = document.createElement("AgentParams");
		for (int i = 0; i < agentParams.length; i++) {
			Node n = document.createElement("Agent" + (i + 1));
			agentParams[i].saveConfig(n, document);
			trans.appendChild(n);
		}
		root.appendChild(trans);
	}

	public void resize(int agentTypes) {
		GeneticStateAgentParams[] n = Arrays.copyOf(agentParams, agentTypes);

		for (int i = agentParams.length; i < agentTypes; i++) {
			n[i] = new GeneticStateAgentParams(simParam);
		}
		agentParams = n;
		typeCount = agentTypes;
	}

}
