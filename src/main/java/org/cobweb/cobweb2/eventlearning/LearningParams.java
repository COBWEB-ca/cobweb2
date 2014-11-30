package org.cobweb.cobweb2.eventlearning;

import java.util.Arrays;

import org.cobweb.cobweb2.interconnect.AgentFoodCountable;
import org.cobweb.cobweb2.io.CobwebParam;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class LearningParams implements CobwebParam {
	private static final long serialVersionUID = 2682098543563943839L;

	private LearningAgentParams[] learningParams;

	private final AgentFoodCountable env;

	public LearningAgentParams[] getLearningAgentParams() {
		return learningParams;
	}

	public LearningParams(AgentFoodCountable env) {
		this.env = env;

		learningParams = new LearningAgentParams[env.getAgentTypes()];

		for (int i = 0; i < env.getAgentTypes(); i++) {
			learningParams[i] = new LearningAgentParams();
			learningParams[i].type = i;
		}
	}	

	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		learningParams = new LearningAgentParams[env.getAgentTypes()];

		NodeList nodeList = root.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (n.getNodeName().equals("AgentParams")) {
				NodeList nodeList2 = n.getChildNodes();
				for (int j = 0; j < nodeList2.getLength(); j++) {
					if (j >= env.getAgentTypes()) {
						break;
					}
					Node paramNode = nodeList2.item(j);
					learningParams[j] = new LearningAgentParams();
					learningParams[j].loadConfig(paramNode);
				}
			}	
		}

		for (int i = 0; i < learningParams.length; i++) {
			if (learningParams[i] == null) {
				learningParams[i] = new LearningAgentParams();
			}
		}
	}

	@Override
	public void saveConfig(Node root, Document document) {
		Node params = document.createElement("AgentParams");
		for (int i = 0; i < learningParams.length; i++) {
			Node n = document.createElement("Agent" + (i + 1));
			learningParams[i].saveConfig(n, document);
			params.appendChild(n);
		}
		root.appendChild(params);
	}

	public void resize(AgentFoodCountable envParams) {
		LearningAgentParams[] n = Arrays.copyOf(learningParams, envParams.getAgentTypes());

		for (int i = learningParams.length; i < envParams.getAgentTypes(); i++) {
			n[i] = new LearningAgentParams();
		}
		learningParams = n;
	}	
}
