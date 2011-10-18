package cwcore;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cobweb.params.CobwebParam;


public class GeneticStateParams implements CobwebParam {


	private static final long serialVersionUID = -7136336946033726870L;

	public GeneticStateAgentParams[] agentParams;

	int typeCount = 4;

	private static final Pattern agentRE = Pattern.compile("^Agent(\\d+)$");

	GeneticStateParams() {
		agentParams = new GeneticStateAgentParams[4];
		init();
	}

	private void init() {
		for (int i = 0; i < agentParams.length; i++) {
			agentParams[i] = new GeneticStateAgentParams();
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

				GeneticStateAgentParams ss = new GeneticStateAgentParams();
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

	public void setTypeCount(int count) {
		this.typeCount = count;
	}

	public void resize(int agentTypes) {
		GeneticStateAgentParams[] n = Arrays.copyOf(agentParams, agentTypes);

		for (int i = agentParams.length; i < agentTypes; i++) {
			n[i] = new GeneticStateAgentParams();
		}
		agentParams = n;
	}

}
