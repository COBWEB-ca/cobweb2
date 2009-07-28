package temperature;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cobweb.params.CobwebParam;
import cobweb.params.ConfDisplayName;
import cwcore.complexParams.AgentFoodCountable;

public class TemperatureParams implements CobwebParam {

	private static final long serialVersionUID = -4024670457346662550L;

	@ConfDisplayName("Temperature Band")
	public int[] tempBands = new int[5];

	public TemperatureAgentParams[] agentParams;

	private final AgentFoodCountable env;
	
	public TemperatureParams(AgentFoodCountable env) {
		this.env = env;
		
		this.agentParams = new TemperatureAgentParams[env.getAgentTypes()];
		
		for (int i = 0; i < agentParams.length; i++) {
			agentParams[i] = new TemperatureAgentParams();
		}
	}
	
	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		
		agentParams = new TemperatureAgentParams[env.getAgentTypes()];
		
		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName().equals("TempBands")) {
				NodeList nl2 = n.getChildNodes();
				for (int j = 0; j < nl2.getLength(); j++) {
					Node tt = nl2.item(j);
					tempBands[j] = Integer.parseInt(tt.getTextContent());
				}
			} else if (n.getNodeName().equals("AgentParams")) {
				NodeList nl2 = n.getChildNodes();
				for (int j = 0; j < nl2.getLength(); j++) {
					Node tt = nl2.item(j);
					agentParams[j] = new TemperatureAgentParams();
					agentParams[j].loadConfig(tt);
				}
			}
		}
	}
	
	@Override
	public void saveConfig(Node root, Document document) {
		Node bands = document.createElement("TempBands");
		for (int i = 0; i < tempBands.length; i++) {
			Node n = document.createElement("Band" + (i + 1));
			n.setTextContent(Integer.toString(tempBands[i]));
			bands.appendChild(n);
		}
		root.appendChild(bands);
		Node agents = document.createElement("AgentParams");
		for (int i = 0; i < agentParams.length; i++) {
			Node n = document.createElement("Agent" + (i + 1));
			agentParams[i].saveConfig(n, document);
			agents.appendChild(n);
		}
		root.appendChild(agents);
	}
}
