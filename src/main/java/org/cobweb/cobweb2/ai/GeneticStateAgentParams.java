package org.cobweb.cobweb2.ai;

import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.core.params.SimulationParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ParameterCustomSerializable;
import org.cobweb.io.ParameterSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GeneticStateAgentParams implements ParameterCustomSerializable {

	private static final long serialVersionUID = -6295295048720208502L;

	private static final String STATE_ELEMENT = "State";
	private static final String STATES_ELEMENT = "StateSize";

	@ConfDisplayName("state")
	public List<StateSize> stateSizes = new LinkedList<StateSize>();

	public GeneticStateAgentParams(SimulationParams simParam) {
		for(String p : simParam.getPluginParameters()) {
			StateSize ss = new StateSize();
			ss.name = p;
			ss.size = 0;
			stateSizes.add(ss);
		}
	}


	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		stateSizes = new LinkedList<StateSize>();

		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (!n.getNodeName().equals(STATES_ELEMENT))
				continue;

			NodeList nl2 = n.getChildNodes();
			for (int j = 0; j < nl2.getLength(); j++) {
				Node tt = nl2.item(j);
				String name = tt.getNodeName();

				if (!name.equals(STATE_ELEMENT))
					continue;

				StateSize ss = new StateSize();
				ParameterSerializer.load(ss, tt);
				stateSizes.add(ss);
			}
		}
	}

	@Override
	public void saveConfig(Node root, Document document) {
		Node trans = document.createElement(STATES_ELEMENT);
		for (StateSize ss : stateSizes) {
			Node n = document.createElement(STATE_ELEMENT);
			ParameterSerializer.save(ss, n, document);
			trans.appendChild(n);
		}
		root.appendChild(trans);
	}
}
