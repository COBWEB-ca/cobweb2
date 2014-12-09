package org.cobweb.cobweb2.io;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import org.cobweb.io.ConfDynamicInstance;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.ReflectionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains the methods necessary to extract data from the
 * fields in the simulation configuration files.
 * 
 * @author ???
 */
public abstract class AbstractReflectionParams implements CobwebParam {
	private static final long serialVersionUID = -710912579485485125L;

	/**
	 * Allows the extraction of data from a configuration
	 * file for any Cobweb parameters.  The data is passed in as the
	 * root node of a tree containing the data.
	 * 
	 * @param obj The type of object parameters.
	 * @param config The root node of the tree.
	 */
	private static void loadTaggedFields(CobwebParam obj, Node config) {
		Class<?> T = obj.getClass();

		Map<String, Field> fields = new LinkedHashMap<String, Field>();
		for (Field f : T.getFields()) {
			ConfXMLTag tagname = f.getAnnotation(ConfXMLTag.class);
			if (tagname != null) {
				fields.put(tagname.value(), f);
			}
		}

		NodeList children = config.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			Field f = fields.get(n.getNodeName());
			if (f == null)
				continue;
			try {
				if (n.getFirstChild() == null) {
					continue;
				}
				String strVal = n.getFirstChild().getNodeValue();
				Class<?> t = f.getType();
				if (t.isPrimitive() || t.equals(String.class)) {
					f.set(obj, ReflectionUtil.stringToBoxed(t, strVal));

				} else if (CobwebParam.class.isAssignableFrom(t)) {
					ConfDynamicInstance din = f.getAnnotation(ConfDynamicInstance.class);
					if (din != null) {
						CobwebParam inner = din.value().newInstance().instantiate(obj);
						f.set(obj, inner);
					}

					CobwebParam inner = (CobwebParam) f.get(obj);
					inner.loadConfig(n);

				} else {
					throw new IllegalArgumentException("Unknown field type");
				}
			} catch (Exception ex) {
				throw new IllegalArgumentException("Cannot load configuration field: " + f.getName(), ex);
			}
		}
	}

	/**
	 * Saves data fields from any object implementing the CobwebParam interface to a
	 * data file, doc.
	 * 
	 * @param obj Cobweb parameter object.
	 * @param config Initial node to add data fields to.
	 * @param doc Data file fields are saved to.
	 */
	private static void saveTaggedFields(CobwebParam obj, Node config, Document doc) {
		Class<?> T = obj.getClass();

		for (Field f : T.getFields()) {
			Class<?> t = f.getType();

			ConfXMLTag tagname = f.getAnnotation(ConfXMLTag.class);

			if (tagname == null)
				continue;

			Element tag = doc.createElement(tagname.value());
			try {
				String value = null;
				if (t.isPrimitive() || t.equals(String.class)) {
					value = f.get(obj).toString();

				} else if (CobwebParam.class.isAssignableFrom(t)) {
					CobwebParam inner = (CobwebParam) f.get(obj);
					inner.saveConfig(tag, doc);

				} else {
					throw new IllegalArgumentException("Unknown field type");
				}

				if (value != null)
					tag.setTextContent(value);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Cannot save configuration field: " + f.getName(), ex);
			}
			config.appendChild(tag);
		}
	}

	/**
	 * Calls loadTaggedFields using the Cobweb parameter type.
	 * 
	 * @param root The root node of the data tree.
	 * @see AbstractReflectionParams#loadTaggedFields(CobwebParam, Node)
	 */
	@Override
	public void loadConfig(Node root) throws IllegalArgumentException {
		loadTaggedFields(this, root);
	}

	/**
	 * Calls saveTaggedFields using the Cobweb parameter type.
	 * 
	 * @param root The root node of the data tree.
	 * @param document The document that the data will be saved to.
	 * @see AbstractReflectionParams#saveTaggedFields(CobwebParam, Node, Document)
	 */
	@Override
	public void saveConfig(Node root, Document document) {
		saveTaggedFields(this, root, document);
	}
}