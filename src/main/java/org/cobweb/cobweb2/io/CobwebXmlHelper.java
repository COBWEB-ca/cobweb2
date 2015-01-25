package org.cobweb.cobweb2.io;

import java.io.OutputStream;

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

import org.cobweb.util.Versionator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class CobwebXmlHelper {

	/**
	 * Creates an XML document with given root name and namespace.
	 * The namespace is nested within the cobweb2 namespace.
	 * Cobweb version is included as an attribute.
	 *
	 * @param rootName name of root element
	 * @param rootSchema sub-namespace for root element
	 * @return root Element of the new Document. Use root.getOwnerDocument() to get the document
	 */
	public static Element createDocument(String rootName, String rootSchema) {
		Document d;
		try {
			d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		Element root = d.createElementNS("http://cobweb.ca/schema/cobweb2/" + rootSchema, rootName);
		root.setAttribute("cobweb-version", Versionator.getVersion());
		d.appendChild(root);

		return root;
	}

	public static void writeDocument(OutputStream stream, Document d) {
		Source s = new DOMSource(d);

		Transformer t;
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			t = tf.newTransformer();

		} catch (TransformerConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.STANDALONE, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		Result r = new StreamResult(stream);
		try {
			t.transform(s, r);
		} catch (TransformerException ex) {
			throw new RuntimeException(ex);
		}
	}

}
