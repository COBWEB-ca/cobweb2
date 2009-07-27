/**
 *
 */
package cwcore.complexParams;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cobweb.params.CobwebParam;
import cobweb.params.ConfDisplayName;

public class FoodwebParams implements CobwebParam {

	private static final long serialVersionUID = 1380425322335531943L;

	@ConfDisplayName("Agent ")
	public boolean[] canEatAgent;

	@ConfDisplayName("Food ")
	public boolean[] canEatFood;

	private final AgentFoodCountable env;

	private static final Pattern AgentN = Pattern.compile("agent(\\d+)");
	private static final Pattern FoodN = Pattern.compile("food(\\d+)");

	public FoodwebParams(AgentFoodCountable env) {
		this.env = env;

		canEatFood = new boolean[env.getFoodTypes()];
		canEatAgent = new boolean[env.getAgentTypes()];
		for (int i = 0; i < canEatFood.length; i++) {
			canEatFood[i] = true;
		}
	}

	public void loadConfig(Node root) throws IllegalArgumentException {
		canEatFood = new boolean[env.getFoodTypes()];
		canEatAgent = new boolean[env.getAgentTypes()];

		NodeList nodes = root.getChildNodes();

		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			String name = n.getNodeName();
			Matcher m;
			if ((m = AgentN.matcher(name)).matches()) {
				int agent = Integer.parseInt(m.group(1)) - 1;
				canEatAgent[agent] = Boolean.parseBoolean(n.getFirstChild().getNodeValue());
			} else if ((m = FoodN.matcher(name)).matches()) {
				int food = Integer.parseInt(m.group(1)) - 1;
				canEatFood[food] = Boolean.parseBoolean(n.getFirstChild().getNodeValue());
			}
		}
	}

	public void saveConfig(Node root, Document document) {
		for (int agent = 0; agent < env.getAgentTypes(); agent++) {
			Node n = document.createElement("agent" + (agent + 1));
			n.setTextContent(Boolean.toString(canEatAgent[agent]));
			root.appendChild(n);
		}
		for (int food = 0; food < env.getFoodTypes(); food++) {
			Node n = document.createElement("food" + (food + 1));
			n.setTextContent(Boolean.toString(canEatFood[food]));
			root.appendChild(n);
		}
	}

}