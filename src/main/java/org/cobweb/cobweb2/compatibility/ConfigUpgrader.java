package org.cobweb.cobweb2.compatibility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.cobweb.cobweb2.ai.GeneticController;
import org.cobweb.cobweb2.ai.LinearWeightsController;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.params.ComplexEnvironmentParams;
import org.cobweb.cobweb2.eventlearning.ComplexAgentLearning;
import org.cobweb.cobweb2.eventlearning.ComplexEnvironmentLearning;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class ConfigUpgrader {

	public static void upgrade(ComplexEnvironmentParams envParams) {
		envParams.controllerName = updateClassName(
				envParams.controllerName,
				GeneticController.class,
				LinearWeightsController.class
				);

		envParams.agentName = updateClassName(
				envParams.agentName,
				ComplexAgent.class,
				ComplexAgentLearning.class
				);

		envParams.environmentName = updateClassName(
				envParams.environmentName,
				ComplexEnvironment.class,
				ComplexEnvironmentLearning.class
				);
	}

	private static String updateClassName(String oldName, Class<?>... candidates) {
		try {
			Class.forName(oldName);
			return oldName;
		} catch (ClassNotFoundException ex) {
			for (Class<?> c : candidates) {
				if (oldName.endsWith("." + c.getSimpleName())) {
					return c.getName();
				}
			}
		}

		throw new IllegalArgumentException("Cannot find missing class: " + oldName);
	}

	private static final String[] VERSIONS = {
		"2011",
		"2015-01-14"
	};

	private static final String VERSION_LATEST = VERSIONS[VERSIONS.length - 1];

	public static void upgradeXSLT(File filename) {
		FileInputStream file;
		try {
			file = new FileInputStream(filename);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		Document document = loadDocument(file);

		Element root = document.getDocumentElement();
		String version;

		if (root.getNodeName().equals("COBWEB2Config"))
			version = root.getAttribute("config-version");
		else if (root.getNodeName().equals("inputData"))
			version = VERSIONS[0];
		else
			throw new IllegalArgumentException("This config file is very old and can't be read");

		if (version.compareTo(VERSION_LATEST) >= 0)
			return;

		if (version.equals(VERSIONS[0])) {
			// Upgrade from 2011

			InputStream xsltStream = ClassLoader.getSystemResourceAsStream("compatibility/upgrade-2011-2015-01-14.xslt");

			TransformerFactory factory = TransformerFactory.newInstance();
			try {
				Transformer transformer = factory.newTransformer(new StreamSource(xsltStream));
				StreamResult outputTarget = new StreamResult(new FileOutputStream("converted.xml"));
				transformer.transform(new StreamSource(new FileInputStream(filename)), outputTarget);
			} catch (TransformerConfigurationException ex) {
				throw new RuntimeException(ex);
			} catch (FileNotFoundException ex) {
				throw new RuntimeException(ex);
			} catch (TransformerException ex) {
				throw new RuntimeException(ex);
			} finally {
				try {
					xsltStream.close();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}

		}

	}

	protected static Document loadDocument(InputStream file) {
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
		return document;
	}

}
