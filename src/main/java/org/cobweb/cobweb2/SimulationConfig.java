package org.cobweb.cobweb2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.cobweb.cobweb2.abiotic.TemperatureParams;
import org.cobweb.cobweb2.ai.ControllerParams;
import org.cobweb.cobweb2.ai.GeneticControllerParams;
import org.cobweb.cobweb2.compatibility.ConfigUpgrader;
import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.cobweb2.core.params.ComplexEnvironmentParams;
import org.cobweb.cobweb2.core.params.ComplexFoodParams;
import org.cobweb.cobweb2.core.params.SimulationParams;
import org.cobweb.cobweb2.disease.DiseaseParams;
import org.cobweb.cobweb2.eventlearning.ComplexAgentLearning;
import org.cobweb.cobweb2.eventlearning.LearningParams;
import org.cobweb.cobweb2.genetics.GeneticParams;
import org.cobweb.cobweb2.production.ProductionParams;
import org.cobweb.io.ParameterSerializer;
import org.cobweb.util.Versionator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Used to organize, modify, and access simulation parameters.
 */
public class SimulationConfig implements SimulationParams {
	private static void removeIgnorableWSNodes(Element parent) {
		Node nextNode = parent.getFirstChild();
		for (Node child = parent.getFirstChild(); nextNode != null;) {
			child = nextNode;
			nextNode = child.getNextSibling();
			if (child.getNodeType() == Node.TEXT_NODE) {
				// Checks if the text node is ignorable
				if (child.getTextContent().matches("^\\s*$")) {
					parent.removeChild(child);
				}
			} else if (child.getNodeType() == Node.ELEMENT_NODE) {
				removeIgnorableWSNodes((Element )child);
			}
		}
	}

	private String fileName = null;

	private ComplexEnvironmentParams envParams;

	private GeneticParams geneticParams;

	private ComplexAgentParams[] agentParams;

	private ProductionParams[] prodParams;

	private LearningParams learningParams;

	private ComplexFoodParams[] foodParams;

	private DiseaseParams[] diseaseParams;

	private TemperatureParams tempParams;

	private ControllerParams controllerParams;

	/**
	 * Creates the default Cobweb simulation parameters.
	 */
	public SimulationConfig() {
		envParams = new ComplexEnvironmentParams();

		agentParams = new ComplexAgentParams[envParams.getAgentTypes()];
		for (int i = 0; i < envParams.getAgentTypes(); i++) {
			agentParams[i] = new ComplexAgentParams(envParams);
			agentParams[i].type = i;
		}

		foodParams = new ComplexFoodParams[envParams.getFoodTypes()];
		for (int i = 0; i < envParams.getFoodTypes(); i++) {
			foodParams[i] = new ComplexFoodParams();
			foodParams[i].type = i;
		}

		geneticParams = new GeneticParams(envParams);

		diseaseParams = new DiseaseParams[envParams.getAgentTypes()];
		for (int i = 0; i < envParams.getAgentTypes(); i++) {
			diseaseParams[i] = new DiseaseParams(envParams);
			diseaseParams[i].type = i;
		}

		prodParams = new ProductionParams[envParams.getAgentTypes()];
		for (int i = 0; i < envParams.getAgentTypes(); i++) {
			prodParams[i] = new ProductionParams();
			prodParams[i].type = i;
		}

		tempParams = new TemperatureParams(envParams);

		learningParams = new LearningParams(envParams);

		controllerParams = new GeneticControllerParams(this);

		fileName = "default simulation";
	}

	/**
	 * Constructor that allows input from a file stream to configure simulation parameters.
	 *
	 * @param file Input file stream.
	 */
	public SimulationConfig(InputStream file) {
		this();
		this.fileName = ":STREAM:" + file.toString() + ":";
		loadFile(file);
	}

	/**
	 * Constructor that allows input from a file to configure the simulation parameters.
	 *
	 * @param fileName Name of the file used for simulation configuration.
	 * @see SimulationConfig#loadFile(InputStream)
	 */
	public SimulationConfig(String fileName) throws FileNotFoundException {
		this();
		this.fileName = fileName;
		FileInputStream configStream = new FileInputStream(fileName);
		loadFile(configStream);
		try {
			configStream.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * @return Agent parameters
	 */
	public ComplexAgentParams[] getAgentParams() {
		return agentParams;
	}

	public ProductionParams[] getProdParams() {
		return prodParams;
	}


	/**
	 * @return Disease parameters
	 */
	public DiseaseParams[] getDiseaseParams() {
		return diseaseParams;
	}

	/**
	 * @return Environment parameters
	 */
	public ComplexEnvironmentParams getEnvParams() {
		return envParams;
	}

	/**
	 * @return Simulation configuration file name
	 */
	public String getFilename() {
		return fileName;
	}

	/**
	 * @return Food parameters
	 */
	public ComplexFoodParams[] getFoodParams() {
		return foodParams;
	}

	/**
	 * @return Genetic parameters
	 */
	public GeneticParams getGeneticParams() {
		return geneticParams;
	}

	/**
	 * @return Temperature parameters
	 */
	public TemperatureParams getTempParams() {
		return tempParams;
	}

	public LearningParams getLearningParams() {
		return learningParams;
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
	private void loadFile(InputStream file) throws IllegalArgumentException {

		// read these variables from the xml file

		// DOM initialization
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		// factory.setValidating(true);

		Document document;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(file);
		} catch (SAXException ex) {
			throw new IllegalArgumentException("Can't open config file", ex);
		} catch (ParserConfigurationException ex) {
			throw new IllegalArgumentException("Can't open config file", ex);
		} catch (IOException ex) {
			throw new IllegalArgumentException("Can't open config file", ex);
		}


		Node root = document.getFirstChild();
		removeIgnorableWSNodes((Element) root);

		envParams = new ComplexEnvironmentParams();

		ParameterSerializer.load(envParams, root);

		ConfigUpgrader.upgrade(envParams);

		agentParams = new ComplexAgentParams[envParams.getAgentTypes()];
		prodParams = new ProductionParams[envParams.getAgentTypes()];
		foodParams = new ComplexFoodParams[envParams.getFoodTypes()];
		diseaseParams = new DiseaseParams[envParams.getAgentTypes()];

		for (int i = 0; i < envParams.getAgentTypes(); i++)
			diseaseParams[i] = new DiseaseParams(envParams);

		tempParams = new TemperatureParams(envParams);

		learningParams = new LearningParams(envParams);

		geneticParams = new GeneticParams(envParams);

		NodeList nodes = root.getChildNodes();
		int agent = 0;
		int prod = 0;
		int food = 0;
		for (int j = 0; j < nodes.getLength(); j++) {
			Node node = nodes.item(j);
			String nodeName = node.getNodeName();

			if (nodeName.equals("ga")) {
				ParameterSerializer.load(geneticParams, node);

			} else if (nodeName.equals("agent")) {
				ComplexAgentParams p = new ComplexAgentParams(envParams);
				ParameterSerializer.load(p, node);
				if (p.type < 0)
					p.type = agent++;
				if (p.type >= envParams.getAgentTypes())
					continue;
				agentParams[p.type] = p;
			} else if (nodeName.equals("production")) {
				ProductionParams p = new ProductionParams();
				ParameterSerializer.load(p, node);
				if (p.type < 0)
					p.type = prod++;
				if (p.type >= envParams.getAgentTypes())
					continue;
				prodParams[p.type] = p;
			} else if (nodeName.equals("food")) {
				ComplexFoodParams p = new ComplexFoodParams();
				ParameterSerializer.load(p, node);
				if (p.type < 0)
					p.type = food++;

				if (p.type >= envParams.getFoodTypes())
					continue;

				foodParams[p.type] = p;
			} else if (nodeName.equals("disease")) {
				parseDiseaseParams(node);
			} else if (nodeName.equals("Temperature")) {
				ParameterSerializer.load(tempParams, node);
			} else if (nodeName.equals("Learning")) {
				ParameterSerializer.load(learningParams, node);
			} else if (nodeName.equals("ControllerConfig")){
				// FIXME: this is initialized after everything else because
				// Controllers use SimulationParams.getPluginParameters()
				// and things like disease provide are those plugins
				try {
					controllerParams = (ControllerParams) Class.forName(envParams.controllerName + "Params")
							.getConstructor(SimulationParams.class)
							.newInstance((SimulationParams) this);
					controllerParams.resize(envParams);
				} catch (Exception ex) {
					throw new RuntimeException("Could not set up controller", ex);
				}
				ParameterSerializer.load(controllerParams, node);
			}
		}
		for (int i = 0; i < agentParams.length; i++) {
			if (agentParams[i] == null) {
				agentParams[i] = new ComplexAgentParams(envParams);
				agentParams[i].type = i;
			}
		}
		for (int i = 0; i < prodParams.length; i++) {
			if (prodParams[i] == null) {
				prodParams[i] = new ProductionParams();
				prodParams[i].type = i;
			}
		}
		for (int i = 0; i < foodParams.length; i++) {
			if (foodParams[i] == null) {
				foodParams[i] = new ComplexFoodParams();
				foodParams[i].type = i;
			}
		}

	}

	private void parseDiseaseParams(Node root) {
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (i >= envParams.getAgentTypes())
				break;
			DiseaseParams dp = new DiseaseParams(envParams);
			ParameterSerializer.load(dp, n);
			diseaseParams[i] = dp;
		}
		for (int i = 0; i < diseaseParams.length; i++) {
			if (diseaseParams[i] == null)
				diseaseParams[i] = new DiseaseParams(envParams);
		}
	}

	/**
	 * Writes the information stored in this tree to an XML file, conforming to the rules of our spec.
	 *
	 */
	public void write(OutputStream stream) {
		Document d;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		Node root = d.createElement("inputData");


		ParameterSerializer.save(envParams, root, d);
		for (int i = 0; i < envParams.getAgentTypes(); i++) {
			Node node = d.createElement("agent");
			ParameterSerializer.save(agentParams[i], node, d);
			root.appendChild(node);
		}

		for (int i = 0; i < envParams.getAgentTypes(); i++) {
			Node node = d.createElement("production");
			ParameterSerializer.save(prodParams[i], node, d);
			root.appendChild(node);
		}

		for (int i = 0; i < envParams.getFoodTypes(); i++) {
			Node node = d.createElement("food");
			ParameterSerializer.save(foodParams[i], node, d);
			root.appendChild(node);
		}

		Node ga = d.createElement("ga");
		ParameterSerializer.save(geneticParams, ga, d);

		root.appendChild(ga);

		Node disease = d.createElement("disease");
		for (DiseaseParams diseaseParam : diseaseParams) {
			Node node = d.createElement("agent");
			ParameterSerializer.save(diseaseParam, node, d);
			disease.appendChild(node);
		}
		root.appendChild(disease);

		Node temp = d.createElement("Temperature");
		ParameterSerializer.save(tempParams, temp, d);
		root.appendChild(temp);

		if (this.envParams.agentName.equals(ComplexAgentLearning.class.getName())) {
			Node learn = d.createElement("Learning");
			ParameterSerializer.save(learningParams, learn, d);
			root.appendChild(learn);
		}

		Node controller = d.createElement("ControllerConfig");
		ParameterSerializer.save(controllerParams, controller, d);
		root.appendChild(controller);

		Node version = d.createComment("Generated by COBWEB2 version " + Versionator.getVersion() );
		root.appendChild(version);

		d.appendChild(root);

		Source s = new DOMSource(d);

		Transformer t;
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			t = tf.newTransformer();

		} catch (TransformerConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setParameter(OutputKeys.STANDALONE, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		Result r = new StreamResult(stream);
		try {
			t.transform(s, r);
		} catch (TransformerException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void SetAgentTypeCount(int count) {
		this.envParams.agentTypeCount = count;
		this.envParams.foodTypeCount = count;

		{
			ComplexAgentParams[] n = Arrays.copyOf(this.agentParams, count);
			for (int i = 0; i < this.agentParams.length && i < count; i++) {
				n[i].resize(envParams);
			}
			for (int i = this.agentParams.length; i < count; i++) {
				n[i] = new ComplexAgentParams(envParams);
			}
			this.agentParams = n;
		}

		{
			ComplexFoodParams[] n = Arrays.copyOf(this.foodParams, count);
			for (int i = this.foodParams.length; i < count; i++) {
				n[i] = new ComplexFoodParams();
			}
			this.foodParams = n;
		}

		{
			DiseaseParams[] n = Arrays.copyOf(diseaseParams, count);

			for (int i = 0; i < this.diseaseParams.length && i < count; i++) {
				n[i].resize();
			}

			for (int i = this.diseaseParams.length; i < count; i++) {
				n[i] = new DiseaseParams(envParams);
			}

			this.diseaseParams = n;
		}
		{
			ProductionParams[] n = Arrays.copyOf(prodParams, count);

			for (int i = prodParams.length; i < count; i++) {
				n[i] = new ProductionParams();
				n[i].type = i;
			}
			this.prodParams = n;
		}
		{
			this.geneticParams.resize(envParams);
		}
		{
			this.tempParams.resize(envParams);
		}
		{
			this.learningParams.resize(envParams);
		}
		{
			this.controllerParams.resize(envParams);
		}

	}

	public ControllerParams getControllerParams() {
		return controllerParams;
	}

	public void setControllerParams(ControllerParams params) {
		controllerParams = params;
	}

	public boolean isContinuation() {
		return
				envParams.keepOldAgents ||
				envParams.keepOldArray ||
				envParams.keepOldPackets ||
				envParams.keepOldWaste;
	}

	@Override
	public List<String> getPluginParameters() {
		List<String> result = new ArrayList<String>();
		result.addAll(this.prodParams[0].getStatePluginKeys());
		result.addAll(this.tempParams.getStatePluginKeys());

		return result;
	}

	@Override
	public AgentFoodCountable getCounts() {
		return this.envParams;
	}

}
