package driver;

import ga.GeneticParams;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cwcore.complexParams.ComplexAgentParams;
import cwcore.complexParams.ComplexEnvironmentParams;
import cwcore.complexParams.ComplexFoodParams;

public class Parser {
	private String fileName = null;

	private Document document = null;

	/**
	 * The genetic sequence. Initialize them to a certain sequence for the four agents.
	 */
	public String[][] genetic_sequence = { { "000111100001111000011110", "000111100001111000011110",
			"000111100001111000011110", "000111100001111000011110", "", "", "", "", "", "" } };

	private ComplexEnvironmentParams envParams;

	public Parser(InputStream file) {
		this();
		this.fileName = ":STREAM:" + file.toString() + ":";
		loadFile(file);
	}

	public Parser(String fileName) throws FileNotFoundException {
		this();
		this.fileName = fileName;
		loadFile(new FileInputStream(fileName));
	}

	public Parser() {
		envParams = new ComplexEnvironmentParams();

		agentParams = new ComplexAgentParams[envParams.agentTypeCount];
		for (int i = 0; i < envParams.agentTypeCount; i++) {
			agentParams[i] = new ComplexAgentParams(envParams);
			agentParams[i].type = i;
		}

		foodParams = new ComplexFoodParams[envParams.foodTypeCount];
		for (int i = 0; i < envParams.foodTypeCount; i++) {
			foodParams[i] = new ComplexFoodParams();
			foodParams[i].type = i;
		}

		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	public String getFilename() {
		return fileName;
	}

	private GeneticParams geneticParams;

	private void loadFile(InputStream file) throws IllegalArgumentException {

		// read these variables from the xml file

		// DOM initialization
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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

		envParams = new ComplexEnvironmentParams();

		envParams.loadConfig(root);

		agentParams = new ComplexAgentParams[envParams.agentTypeCount];
		foodParams = new ComplexFoodParams[envParams.foodTypeCount];

		geneticParams = new GeneticParams(envParams);

		NodeList nodes = root.getChildNodes();
		int agent = 0;
		int food = 0;
		for (int j = 0; j < nodes.getLength(); j++) {
			Node node = nodes.item(j);
			String nodeName = node.getNodeName();

			if (nodeName.equals("ga")) {
				geneticParams.loadConfig(node);

			} else if (nodeName.equals("agent")) {
				ComplexAgentParams p = new ComplexAgentParams(envParams);
				p.loadConfig(node);
				if (p.type < 0)
					p.type = agent++;
				p.pdMemory = envParams.pdMemorySize;
				agentParams[p.type] = p;

			} else if (nodeName.equals("food")) {
				ComplexFoodParams p = new ComplexFoodParams();
				p.loadConfig(node);
				if (p.type < 0)
					p.type = food++;
				foodParams[p.type] = p;
			}
		}
	}

	private ComplexAgentParams[] agentParams;
	private ComplexFoodParams[] foodParams;

	public GeneticParams getGeneticParams() {
		return geneticParams;
	}

	public ComplexEnvironmentParams getEnvParams() {
		return envParams;
	}

	public ComplexAgentParams[] getAgentParams() {
		return agentParams;
	}

	public ComplexFoodParams[] getFoodParams() {
		return foodParams;
	}

	public Document getDocument() {
		return document;
	}


	/**
	 * Writes the information stored in this tree to an XML file, conforming to the rules of our spec.
	 *
	 */
	public void write(OutputStream stream) throws IOException {
		Document d;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		Node root = d.createElement("inputData");

		envParams.saveConfig(root, d);
		for (int i = 0; i < envParams.agentTypeCount; i++) {
			Node node = d.createElement("agent");
			agentParams[i].saveConfig(node, d);
			root.appendChild(node);
		}

		for (int i = 0; i < envParams.foodTypeCount; i++) {
			Node node = d.createElement("food");
			foodParams[i].saveConfig(node, d);
			root.appendChild(node);
		}
		d.appendChild(root);


		Source s = new DOMSource(d);

		Transformer t;
		try {
			t = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		t.setParameter("indent", "yes");
		Result r = new StreamResult(stream);
		try {
			t.transform(s, r);
		} catch (TransformerException ex) {
			throw new RuntimeException(ex);
		}
	}

} // Parser
