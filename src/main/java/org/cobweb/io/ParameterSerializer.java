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
	 * @param root The root node of the tree.
	 */
	public static ParameterSerializable load(ParameterSerializable obj, Node root) {
		Class<?> T = obj.getClass();

		Map<String, Field> fields = new LinkedHashMap<String, Field>();
		for (Field f : T.getFields()) {
			ConfXMLTag tagname = f.getAnnotation(ConfXMLTag.class);
			if (tagname != null) {
				fields.put(tagname.value(), f);
			}
		}

		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			Field f = fields.get(n.getNodeName());
			if (f == null)
				continue;
			try {
				if (n.getFirstChild() == null) {
					// FIXME: shouldn't happen?
					continue;
				}

				Object currentValue = f.get(obj);

				Object newValue = loadObject(f.getType(), currentValue, n);

				f.set(obj, newValue);

			} catch (Exception ex) {
				throw new IllegalArgumentException("Cannot load configuration field: " + f.getName(), ex);
			}
		}

		if (obj instanceof ParameterCustomSerializable) {
			ParameterCustomSerializable c = (ParameterCustomSerializable) obj;
			c.loadConfig(root);
		}

		return obj;
	}

	private static Object loadObject(Class<?> type, Object currentValue, Node objectNode) throws IllegalArgumentException, IllegalAccessException {
		Object newValue = currentValue;

		if (isPrimitive(type)) {
			String strVal = objectNode.getFirstChild().getNodeValue();
			newValue = ReflectionUtil.stringToBoxed(type, strVal);

		} else if (canSerializeDirectly(type)) {
			ParameterSerializable inner = (ParameterSerializable) currentValue;
			newValue = load(inner, objectNode);

		} else {
			throw new IllegalArgumentException("Unknown field type");
		}
		return newValue;
	}

	private static void saveObject(Class<?> type, Object value, Element tag, Document doc) {
		if (isPrimitive(type)) {
			tag.setTextContent(value.toString());

		} else if (canSerializeDirectly(type)) {
			ParameterSerializable inner = (ParameterSerializable) value;
			save(inner, tag, doc);

		} else {
			throw new IllegalArgumentException("Unknown field type");
		}
	}

	protected static boolean isPrimitive(Class<?> t) {
		return t.isPrimitive() || t.equals(String.class);
	}

	protected static boolean canSerializeDirectly(Class<?> T) {
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

			ConfXMLTag tagname = f.getAnnotation(ConfXMLTag.class);

			if (tagname == null)
				continue;

			Element tag = doc.createElement(tagname.value());
			try {
				Class<?> t = f.getType();
				Object value = f.get(obj);
				saveObject(t, value, tag, doc);

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
