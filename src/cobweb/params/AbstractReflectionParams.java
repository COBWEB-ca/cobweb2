package cobweb.params;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class AbstractReflectionParams implements CobwebParam {
	private static final long serialVersionUID = -710912579485485125L;

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

	public void loadConfig(Node root) throws IllegalArgumentException {
		loadTaggedFields(this, root);
	}

	public void saveConfig(Node root, Document document) {
		saveTaggedFields(this, root, document);
	}
}