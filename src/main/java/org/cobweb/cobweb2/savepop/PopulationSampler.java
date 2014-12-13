package org.cobweb.cobweb2.savepop;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.Direction;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class PopulationSampler {


	/** Save a sample population as an XML file */
	public static void savePopulation(Simulation sim, String popName, String option, int amount) {

		int totalPop;

		if (option.equals("percentage")) {
			totalPop = sim.theEnvironment.getAgentCount() * amount / 100;
		} else {
			int currPop = sim.theEnvironment.getAgentCount();
			if (amount > currPop) {

				totalPop = currPop;
			} else {

				totalPop = amount;
			}
		}

		Document d;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		Node root = d.createElement("Agents");

		int currentPopCount = 1;

		for (Agent agent : sim.theEnvironment.getAgents()) {
			if (currentPopCount > totalPop)
				break;
			Node node = makeNode((ComplexAgent) agent, d);

			Element locationElement = d.createElement("location");

			{
				Location location = agent.getPosition();
				Element coordinateElement = d.createElement("x");
				coordinateElement.appendChild(d.createTextNode(location.x +""));
				locationElement.appendChild(coordinateElement);

				coordinateElement = d.createElement("y");
				coordinateElement.appendChild(d.createTextNode(location.y +""));
				locationElement.appendChild(coordinateElement);
			}

			node.appendChild(locationElement);

			root.appendChild(node);
			currentPopCount++;

		}

		d.appendChild(root);

		Source s = new DOMSource(d);

		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setParameter(OutputKeys.STANDALONE, "yes");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			FileOutputStream stream = new FileOutputStream(popName);
			Result r = new StreamResult(stream);
			t.transform(s, r);
		} catch (Exception ex) {
			throw new RuntimeException("Couldn't save population", ex);
		}
	}


	public static Node makeNode(ComplexAgent a, Document d) {

		Node agent = d.createElement("Agent");

		Element agentTypeElement = d.createElement("agentType");
		agentTypeElement.appendChild(d.createTextNode(a.getAgentType() +""));
		agent.appendChild(agentTypeElement);


		Element doCheatElement = d.createElement("doCheat");
		doCheatElement.appendChild(d.createTextNode(a.pdCheater +""));
		agent.appendChild(doCheatElement);

		Element paramsElement = d.createElement("params");

		a.params.saveConfig(paramsElement, d);

		agent.appendChild(paramsElement);

		Element directionElement = d.createElement("direction");

		{
			Direction location = a.getPosition().direction;
			Element coordinateElement = d.createElement("x");
			coordinateElement.appendChild(d.createTextNode(location.x +""));
			directionElement.appendChild(coordinateElement);

			coordinateElement = d.createElement("y");
			coordinateElement.appendChild(d.createTextNode(location.y +""));
			directionElement.appendChild(coordinateElement);
		}

		agent.appendChild(directionElement);

		return agent;
	}

	/**
	 * TODO: Check if this works, possibly rewrite
	 * @param fileName path to population file
	 * @param replace delete current population before inserting
	 */
	public static void insertPopulation(Simulation sim, String fileName, boolean replace) {
		if (replace) {
			sim.theEnvironment.clearAgents();
		}

		try {
			FileInputStream file = new FileInputStream(fileName);

			// DOM initialization
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			factory.setIgnoringComments(true);
			// factory.setValidating(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(file);

			NodeList agents = document.getElementsByTagName("Agent");

			for (int i = 0 ; i < agents.getLength(); i++){
				ComplexAgentParams params = new ComplexAgentParams(sim.simulationConfig.getEnvParams());

				Node agent = agents.item(i);
				Element element = (Element) agent;

				NodeList paramsElement = element.getElementsByTagName("params");
				Element paramNode = (Element) paramsElement.item(0);


				//NodeList prodParamsElement = element.getElementsByTagName("prodParams");
				//Element prodParamNode = (Element) prodParamsElement.item(0);

				NodeList agentTypeElement = element.getElementsByTagName("agentType");
				NodeList pdCheaterElement = element.getElementsByTagName("doCheat");

				NodeList directionElement = element.getElementsByTagName("direction");
				Element direction = (Element) directionElement.item(0);
				NodeList coordinates = direction.getElementsByTagName("coordinate");

				NodeList locationElement = element.getElementsByTagName("location");
				Element location = (Element) locationElement.item(0);
				NodeList axisPos = location.getElementsByTagName("axisPos");

				// location
				int [] axis = new int [axisPos.getLength()];
				for (int j = 0 ; j < axisPos.getLength(); j++) {
					axis[j] = Integer.parseInt(axisPos.item(j).getChildNodes().item(0).getNodeValue());
				}

				Location loc = sim.theEnvironment.getLocation(axis[0], axis[1]);

				// direction
				int [] coords = new int [coordinates.getLength()];
				for (int j = 0 ; j < coordinates.getLength(); j++) {
					coords[j] = Integer.parseInt(coordinates.item(j).getChildNodes().item(0).getNodeValue());
				}
				Direction facing = new Direction(coords[0], coords[1]);

				LocationDirection locDir = new LocationDirection(loc, facing);

				// parameters
				params.loadConfig(paramNode);
				//prodParams.loadConfig(prodParamNode);

				// agentType
				int agentType = Integer.parseInt(agentTypeElement.item(0).getChildNodes().item(0).getNodeValue());

				// doCheat
				boolean pdCheater = Boolean.parseBoolean(pdCheaterElement.item(0).getChildNodes().item(0).getNodeValue());


				ComplexAgent cAgent = sim.newAgent();
				cAgent.init(sim.theEnvironment, agentType, locDir, params);
				cAgent.pdCheater = pdCheater;
				sim.theEnvironment.setAgent(loc, cAgent);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Can't open config file", ex);
		}
	}

}
