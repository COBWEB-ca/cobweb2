package org.cobweb.cobweb2.eventlearning;

import java.util.Arrays;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterCustomSerializable;
import org.cobweb.io.ParameterSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class LearningParams implements ParameterCustomSerializable {
	private static final long serialVersionUID = 2682098543563943839L;

	@ConfXMLTag("AgentParams") //FIXME
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
					ParameterSerializer.load(learningParams[j], paramNode);
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
			ParameterSerializer.save(learningParams[i], n, document);
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
