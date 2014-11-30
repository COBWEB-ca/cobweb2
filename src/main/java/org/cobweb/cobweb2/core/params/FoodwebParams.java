/**
 *
 */
package org.cobweb.cobweb2.core.params;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cobweb.cobweb2.interconnect.AgentFoodCountable;
import org.cobweb.cobweb2.io.CobwebParam;
import org.cobweb.cobweb2.io.ConfDisplayName;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Food web parameters for an agent.
 */
public class FoodwebParams implements CobwebParam {

	private static final long serialVersionUID = 1380425322335531943L;

	/**
	 * If this agent can eat Agent type 1 canEatAgents[0] is true.
	 */
	@ConfDisplayName("Agent")
	public boolean[] canEatAgent;

	/**
	 * If this agent can eat Food type 1 canEatFood[0] is true.
	 */
	@ConfDisplayName("Food")
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
				if (agent >= env.getAgentTypes())
					continue;
				canEatAgent[agent] = Boolean.parseBoolean(n.getFirstChild().getNodeValue());
			} else if ((m = FoodN.matcher(name)).matches()) {
				int food = Integer.parseInt(m.group(1)) - 1;
				if (food >= env.getFoodTypes())
					continue;
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

	public void resize(AgentFoodCountable envParams) {
		{
			boolean[] n = Arrays.copyOf(canEatAgent, envParams.getAgentTypes());
			canEatAgent = n;

		}
		{
			boolean[] n = Arrays.copyOf(canEatFood, envParams.getFoodTypes());
			canEatFood = n;
		}
	}

}