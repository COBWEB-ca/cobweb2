package org.cobweb.cobweb2.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.Direction;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexAgentParams;
import org.cobweb.cobweb2.impl.FieldPhenotype;
import org.cobweb.cobweb2.impl.learning.ComplexAgentLearning;
import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.io.ParameterSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Cobweb2Serializer {

	public final ParameterSerializer serializer;


	public final ChoiceCatalog choiceCatalog;

	public Cobweb2Serializer() {
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

		// Load all the @ConfXMLTag params
		serializer.load(conf, root);

		// Correct any missing/extra parameters after the loading
		conf.setAgentTypes(conf.getAgentTypes());

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

		serializer.save(conf, root, d);

		// If learning agents not used, remove the Learning section of config
		if (!conf.agentName.equals(ComplexAgentLearning.class.getName())) {
			root.removeChild(root.getElementsByTagName("Learning").item(0));
		}

		CobwebXmlHelper.writeDocument(stream, d);
	}

	private Node saveAgent(Agent simpleAgent, Document d) {
		ComplexAgent a = (ComplexAgent) simpleAgent;

		Element agent = d.createElement("Agent");
		agent.setAttribute("type", Integer.toString(a.getType() + 1));

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

		Element plugins = d.createElement("Plugins");
		for (Entry<Class<? extends AgentState>, AgentState> e : a.extraState.entrySet()) {
			AgentState pluginState = e.getValue();
			if (pluginState.isTransient())
				continue;

			Element plugin = d.createElement("Plugin");
			plugin.setAttribute("type", e.getKey().getName());
			serializer.save(pluginState, plugin, d);
			plugins.appendChild(plugin);
		}
		agent.appendChild(plugins);

		// FIXME plugin params: production, disease, PD, etc

		return agent;
	}

	public static class AgentSample {
		public int type;
		public ComplexAgentParams params;
		public LocationDirection position;
		public Map<Class<? extends AgentState>, AgentState> plugins = new HashMap<>();
	}

	private AgentSample loadAgent(Element element, AgentFoodCountable size) {
		AgentSample as = new AgentSample();

		as.type = Integer.parseInt(element.getAttribute("type")) - 1;

		Node paramNode = element.getElementsByTagName("params").item(0);
		ComplexAgentParams params = new ComplexAgentParams(size);
		serializer.load(params, paramNode);
		params.resize(size); // Fix up for simulation being different than population
		as.params = params;

		Element location = (Element)element.getElementsByTagName("location").item(0);
		Location loc = new Location(
				Integer.parseInt(location.getAttribute("x")),
				Integer.parseInt(location.getAttribute("y")));

		Element direction = (Element)element.getElementsByTagName("direction").item(0);
		Direction facing = new Direction(
				Integer.parseInt(direction.getAttribute("x")),
				Integer.parseInt(direction.getAttribute("y")));

		as.position = new LocationDirection(loc, facing);

		NodeList pluginNodes = element.getElementsByTagName("Plugins").item(0).getChildNodes();
		for (int i = 0; i < pluginNodes.getLength(); i++) {
			Element pluginNode = (Element) pluginNodes.item(i);
			String type = pluginNode.getAttribute("type");
			try {
				@SuppressWarnings("unchecked")
				Class<? extends AgentState> pluginType = (Class<? extends AgentState>) Class.forName(type);

				AgentState state = pluginType.newInstance();

				serializer.load(state, pluginNode);

				as.plugins.put(pluginType, state);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
		}

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
