package org.cobweb.io;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import org.cobweb.util.ReflectionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ParameterSerializer {


	/**
	 * Allows the extraction of data from a configuration
	 * file for any Cobweb parameters.  The data is passed in as the
	 * root node of a tree containing the data.
	 *
	 * @param obj The type of object parameters.
	 * @param config The root node of the tree.
	 */
	public static void load(ParameterSerializable obj, Node config) {
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

				} else if (canSerialize(t)) {
					ParameterSerializable inner = (ParameterSerializable) f.get(obj);
					load(inner, n);

				} else {
					throw new IllegalArgumentException("Unknown field type");
				}
			} catch (Exception ex) {
				throw new IllegalArgumentException("Cannot load configuration field: " + f.getName(), ex);
			}
		}

		if (obj instanceof ParameterCustomSerializable) {
			ParameterCustomSerializable c = (ParameterCustomSerializable) obj;
			c.loadConfig(config);
		}
	}

	protected static boolean canSerialize(Class<?> T) {
		return ParameterSerializable.class.isAssignableFrom(T);
	}

	/**
	 * Saves data fields from any object implementing the CobwebParam interface to a
	 * data file, doc.
	 *
	 * @param obj Cobweb parameter object.
	 * @param config Initial node to add data fields to.
	 * @param doc Data file fields are saved to.
	 */
	public static void save(ParameterSerializable obj, Node config, Document doc) {
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

				} else if (canSerialize(t)) {
					ParameterSerializable inner = (ParameterSerializable) f.get(obj);
					save(inner, tag, doc);

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


		if (obj instanceof ParameterCustomSerializable) {
			ParameterCustomSerializable c = (ParameterCustomSerializable) obj;
			c.saveConfig(config, doc);
		}
	}

}
