package org.cobweb.cobweb2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Direction;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.impl.AgentFoodCountable;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexAgentParams;
import org.cobweb.cobweb2.impl.ControllerParams;
import org.cobweb.cobweb2.impl.FieldPhenotype;
import org.cobweb.cobweb2.impl.SimulationParams;
import org.cobweb.cobweb2.impl.learning.ComplexAgentLearning;
import org.cobweb.cobweb2.io.CobwebXmlHelper;
import org.cobweb.cobweb2.io.ConfigUpgrader;
import org.cobweb.cobweb2.plugins.food.ComplexFoodParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.io.ParameterSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SimulationConfigSerializer {

	public final ParameterSerializer serializer;


	public final ChoiceCatalog choiceCatalog;

	public SimulationConfigSerializer() {
		choiceCatalog = new ChoiceCatalog();
		choiceCatalog.addChoice(Phenotype.class, new NullPhenotype());
		for(Phenotype x : FieldPhenotype.getPossibleValues()) {
			choiceCatalog.addChoice(Phenotype.class, x);
		}
		serializer = new ParameterSerializer(choiceCatalog);
	}

	/**
	 * Constructor that allows input from a file stream to configure simulation parameters.
	 *
	 * @param file Input file stream.
	 */
	public SimulationConfig loadConfig(InputStream file) {
		SimulationConfig res = loadFile(file);
		res.fileName = ":STREAM:" + file.toString() + ":";
		return res;
	}

	/**
	 * Constructor that allows input from a file to configure the simulation parameters.
	 *
	 * @param fileName Name of the file used for simulation configuration.
	 */
	public SimulationConfig loadConfig(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		ConfigUpgrader.upgradeConfigFile(file);
		FileInputStream configStream = new FileInputStream(file);
		SimulationConfig res = loadFile(configStream);
		res.fileName = fileName;
		try {
			configStream.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return res;
	}


	/**
	 * This method extracts data from the simulation configuration file and
	 * loads the data into the simulation parameters.  It does this by first
	 * creating a tree that holds all data from file using the DocumentBuilder
	 * class.  Next, the root node of the tree is passed to the
	 * AbstractReflectionParams.loadConfig(Node) method for processing.  This
	 * processing allows the ConfXMLTags to overwrite the default parameters
	 * used when constructing Cobweb environment parameters.
	 *
	 * <p>Once the environment parameters have been extracted successfully,
	 * the rest of the Cobweb parameters can be set (temperature, genetics,
	 * agents, etc.) using the environment parameters.
	 *
	 * @param file The current simulation configuration file.
	 * @see javax.xml.parsers.DocumentBuilder
	 * @throws IllegalArgumentException Unable to open the simulation configuration file.
	 */
	private SimulationConfig loadFile(InputStream file) throws IllegalArgumentException {
		Node root = CobwebXmlHelper.openDocument(file);
		SimulationConfig conf = new SimulationConfig();

		serializer.load(conf.envParams, root);
		ConfigUpgrader.upgrade(conf.envParams);

		// Reset all the settings that depend on agent type count
		conf.SetAgentTypeCount(conf.envParams.getAgentTypes());

		NodeList nodes = root.getChildNodes();
		int agentIndex = 0;
		int foodIndex = 0;
		for (int j = 0; j < nodes.getLength(); j++) {
			Node node = nodes.item(j);
			String nodeName = node.getNodeName();

			if (nodeName.equals("agent")) {
				ComplexAgentParams p = new ComplexAgentParams(conf.envParams);
				serializer.load(p, node);
				if (agentIndex >= conf.envParams.getAgentTypes())
					continue;
				conf.agentParams[agentIndex++] = p;

			} else if (nodeName.equals("food")) {
				ComplexFoodParams p = new ComplexFoodParams();
				serializer.load(p, node);
				if (p.type < 0)
					p.type = foodIndex++;
				if (p.type >= conf.envParams.getFoodTypes())
					continue;
				conf.foodParams[p.type] = p;

			} else if (nodeName.equals("Waste")) {
				serializer.load(conf.wasteParams, node);
			} else if (nodeName.equals("Production")) {
				serializer.load(conf.prodParams, node);
			} else if (nodeName.equals("Temperature")) {
				serializer.load(conf.tempParams, node);
			} else if (nodeName.equals("Learning")) {
				serializer.load(conf.learningParams, node);
			} else if (nodeName.equals("Disease")) {
				serializer.load(conf.diseaseParams, node);
			} else if (nodeName.equals("ga")) {
				serializer.load(conf.geneticParams, node);
			} else if (nodeName.equals("ControllerConfig")){
				// FIXME: this is initialized after everything else because
				// Controllers use SimulationParams.getPluginParameters()
				// and things like disease provide are those plugins
				try {
					conf.controllerParams = (ControllerParams) Class.forName(conf.envParams.controllerName + "Params")
							.getConstructor(SimulationParams.class)
							.newInstance((SimulationParams) conf);
					conf.controllerParams.resize(conf.envParams);
				} catch (InstantiationError | ClassNotFoundException | NoSuchMethodException |
						InstantiationException | IllegalAccessException | InvocationTargetException ex) {
					throw new RuntimeException("Could not set up controller", ex);
				}
				serializer.load(conf.controllerParams, node);
			}
		}

		// Correct any missing/extra parameters after the loading
		conf.SetAgentTypeCount(conf.envParams.getAgentTypes());

		return conf;
	}

	/**
	 * Writes the information stored in this tree to an XML file, conforming to the rules of our spec.
	 *
	 */
	public void saveConfig(SimulationConfig conf, OutputStream stream) {
		Element root = CobwebXmlHelper.createDocument("COBWEB2Config", "config");
		Document d = root.getOwnerDocument();

		root.setAttribute("config-version", "2015-01-14");

		serializer.save(conf.envParams, root, d);
		for (int i = 0; i < conf.envParams.getAgentTypes(); i++) {
			Element node = d.createElement("agent");
			serializer.save(conf.agentParams[i], node, d);
			root.appendChild(node);
		}

		for (int i = 0; i < conf.envParams.getFoodTypes(); i++) {
			Element node = d.createElement("food");
			serializer.save(conf.foodParams[i], node, d);
			root.appendChild(node);
		}

		Element waste = d.createElement("Waste");
		serializer.save(conf.wasteParams, waste, d);
		root.appendChild(waste);

		Element prod = d.createElement("Production");
		serializer.save(conf.prodParams, prod, d);
		root.appendChild(prod);

		Element temp = d.createElement("Temperature");
		serializer.save(conf.tempParams, temp, d);
		root.appendChild(temp);

		if (conf.envParams.agentName.equals(ComplexAgentLearning.class.getName())) {
			Element learn = d.createElement("Learning");
			serializer.save(conf.learningParams, learn, d);
			root.appendChild(learn);
		}

		Element disease = d.createElement("Disease");
		serializer.save(conf.diseaseParams, disease, d);
		root.appendChild(disease);

		Element ga = d.createElement("ga");
		serializer.save(conf.geneticParams, ga, d);
		root.appendChild(ga);

		Element controller = d.createElement("ControllerConfig");
		serializer.save(conf.controllerParams, controller, d);
		root.appendChild(controller);

		CobwebXmlHelper.writeDocument(stream, d);
	}

	private Node saveAgent(Agent simpleAgent, Document d) {
		ComplexAgent a = (ComplexAgent) simpleAgent;

		Element agent = d.createElement("Agent");
		agent.setAttribute("type", Integer.toString(a.getType()));

		Element doCheatElement = d.createElement("doCheat");
		doCheatElement.appendChild(d.createTextNode(a.pdCheater + ""));
		agent.appendChild(doCheatElement);

		Element paramsElement = d.createElement("params");

		serializer.save(a.params, paramsElement, d);

		agent.appendChild(paramsElement);

		{
			Element locationElement = d.createElement("location");
			Location location = a.getPosition();
			locationElement.setAttribute("x", location.x + "");
			locationElement.setAttribute("y", location.y + "");
			agent.appendChild(locationElement);
		}

		{
			Element directionElement = d.createElement("direction");
			Direction direction = a.getPosition().direction;
			directionElement.setAttribute("x", direction.x + "");
			directionElement.setAttribute("y", direction.y + "");
			agent.appendChild(directionElement);
		}

		// FIXME plugin params: production, disease, etc

		return agent;
	}

	public static class AgentSample {
		public int type;
		public ComplexAgentParams params;
		public boolean pdCheater;
		public LocationDirection position;
	}

	private AgentSample loadAgent(Element element, AgentFoodCountable size) {
		AgentSample as = new AgentSample();

		as.type = Integer.parseInt(element.getAttribute("type"));

		Node paramNode = element.getElementsByTagName("params").item(0);
		ComplexAgentParams params = new ComplexAgentParams(size);
		serializer.load(params, paramNode);
		params.resize(size); // Fix up for simulation being different than population
		as.params = params;

		as.pdCheater = Boolean.parseBoolean(element.getElementsByTagName("doCheat").item(0).getTextContent());

		Element location = (Element)element.getElementsByTagName("location").item(0);
		Location loc = new Location(
				Integer.parseInt(location.getAttribute("x")),
				Integer.parseInt(location.getAttribute("y")));

		Element direction = (Element)element.getElementsByTagName("direction").item(0);
		Direction facing = new Direction(
				Integer.parseInt(direction.getAttribute("x")),
				Integer.parseInt(direction.getAttribute("y")));

		as.position = new LocationDirection(loc, facing);

		// FIXME plugin params: production, disease, etc

		return as;
	}

	public Collection<AgentSample> loadAgents(InputStream file, AgentFoodCountable size) {
		Element root = CobwebXmlHelper.openDocument(file);
		NodeList agents = root.getChildNodes();
		Collection<AgentSample> result = new ArrayList<>(agents.getLength());

		for (int i = 0 ; i < agents.getLength(); i++){
			Element agentRoot = (Element) agents.item(i);

			AgentSample as = loadAgent(agentRoot, size);

			result.add(as);
		}
		return result;
	}


	public void serializeAgents(Collection<Agent> agents, OutputStream file) {
		Element root = CobwebXmlHelper.createDocument("PopulationSample", "population");
		Document d = root.getOwnerDocument();
		root.setAttribute("population-version", "2015-01-14");

		for (Agent agent : agents) {
			Node node = saveAgent(agent, d);
			root.appendChild(node);
		}

		CobwebXmlHelper.writeDocument(file, d);
	}

}
