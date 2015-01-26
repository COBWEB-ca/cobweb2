package org.cobweb.cobweb2.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.cobweb2.impl.ComplexEnvironmentParams;
import org.cobweb.cobweb2.impl.ai.GeneticController;
import org.cobweb.cobweb2.impl.ai.LinearWeightsController;
import org.cobweb.cobweb2.impl.learning.ComplexAgentLearning;
import org.cobweb.cobweb2.impl.learning.ComplexEnvironmentLearning;
import org.cobweb.util.Versionator;
import org.w3c.dom.Element;

/**
 * Fixes for dealing with difference between older versions of configuration files.
 */
class ConfigUpgrader {

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

	public static void upgradeConfigFile(File filename) {
		FileInputStream file;
		try {
			file = new FileInputStream(filename);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		Element root = CobwebXmlHelper.openDocument(file);
		try {
			file.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		String version;

		if (root.getNodeName().equals("COBWEB2Config"))
			// new root element includes version number
			version = root.getAttribute("config-version");

		else if (
				root.getNodeName().equals("inputData") ||
				// broken in 2.2009-09-29-3-g23f2baf, fixed in 2.0-34-g4db7bf2
				root.getNodeName().equals("inputeData"))
			version = VERSIONS[0];

		else
			throw new IllegalArgumentException("This config file is very old and can't be read");

		if (version.compareTo(VERSION_LATEST) >= 0)
			return;

		if (version.equals(VERSIONS[0])) {
			// Upgrade from 2011
			upgradeUsingXSLT(filename, "upgrade-2011-2015-01-14.xslt");
		}

	}

	private static void upgradeUsingXSLT(File filename, String xsltName) throws TransformerFactoryConfigurationError {
		// backup file
		File bakFile = new File(filename + ".bak");
		for (int attempt = 1; bakFile.exists(); attempt++) {
			bakFile = new File(filename + ".bak" + attempt);
		}
		filename.renameTo(bakFile);

		InputStream inStream = null;
		try {
			inStream = new FileInputStream(bakFile);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(filename);
		} catch (FileNotFoundException ex) {
			try { inStream.close(); } catch (IOException ex2) {}
			throw new RuntimeException(ex);
		}

		InputStream xsltStream = null;
		try {
			xsltStream = ClassLoader.getSystemResourceAsStream("compatibility/" + xsltName);

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(xsltStream));
			transformer.setParameter("cobweb-version", Versionator.getVersion());

			transformer.transform(new StreamSource(inStream), new StreamResult(outStream));
		} catch (TransformerConfigurationException ex) {
			throw new RuntimeException(ex);
		} catch (TransformerException ex) {
			throw new RuntimeException(ex);
		} finally {
			try { inStream.close(); } catch (IOException ex2) {}
			try { outStream.close(); } catch (IOException ex2) {}
			try { if (xsltStream != null) xsltStream.close(); } catch (IOException ex2) {}
		}
	}

}
