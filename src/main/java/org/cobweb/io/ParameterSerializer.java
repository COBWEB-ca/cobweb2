package org.cobweb.io;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cobweb.util.ReflectionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ParameterSerializer {

	private ChoiceCatalog parts;

	public ParameterSerializer(ChoiceCatalog parts) {
		this.parts = parts;
	}

	/**
	 * Allows the extraction of data from a configuration
	 * file for any Cobweb parameters.  The data is passed in as the
	 * root node of a tree containing the data.
	 *
	 * @param obj The type of object parameters.
	 * @param root The root node of the tree.
	 */
	public ParameterSerializable load(ParameterSerializable obj, Node root) {
		Class<?> T = obj.getClass();

		Map<String, Field> fields = new LinkedHashMap<String, Field>();
		List<Field> squishFields = new ArrayList<Field>();
		for (Field f : T.getFields()) {
			ConfXMLTag tagname = f.getAnnotation(ConfXMLTag.class);
			if (f.isAnnotationPresent(ConfSquishParent.class))
				squishFields.add(f);
			else if (tagname != null) {
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

				Object currentValue = f.get(obj);

				Object newValue = loadObject(f.getType(), f, currentValue, n);

				f.set(obj, newValue);

			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new IllegalArgumentException("Cannot load configuration field: " + f.getName(), ex);
			}
		}

		for (Field f : squishFields) {
			try {
				Object currentValue = f.get(obj);
				Object newValue = loadObject(f.getType(), f, currentValue, root);
				f.set(obj, newValue);
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new IllegalArgumentException("Cannot load configuration field: " + f.getName(), ex);
			}
		}

		return obj;
	}

	/**
	 * Saves data fields from any object implementing the CobwebParam interface to a
	 * data file, doc.
	 *
	 * @param obj Cobweb parameter object.
	 * @param config Initial node to add data fields to.
	 * @param doc Data file fields are saved to.
	 */
	public void save(ParameterSerializable obj, Element config, Document doc) {
		Class<?> T = obj.getClass();

		for (Field f : T.getFields()) {

			boolean squish = f.isAnnotationPresent(ConfSquishParent.class);
			ConfXMLTag tagname = f.getAnnotation(ConfXMLTag.class);

			Element tag;
			if (squish) // store config in current node, don't create a child
				tag = config;
			else if (tagname != null) // store config in child node
				tag = doc.createElement(tagname.value());
			else // not a field we care about
				continue;

			try {
				Class<?> t = f.getType();
				Object value = f.get(obj);
				saveObject(t, f, value, tag, doc);

			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new IllegalArgumentException("Cannot save configuration field: " + f.getName(), ex);
			}

			if (!squish)
				config.appendChild(tag);
		}
	}


	private Object loadObject(Class<?> type, AnnotatedElement annotationSource, Object currentValue, Node objectNode) throws IllegalArgumentException, IllegalAccessException {
		Object newValue = currentValue;

		if (isPrimitive(type)) {
			String strVal = objectNode.getFirstChild().getNodeValue();
			newValue = ReflectionUtil.stringToBoxed(type, strVal);

		} else if (type.isEnum()) {
			newValue = loadEnum(type, objectNode.getFirstChild().getNodeValue());

		} else if (ParameterChoice.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			Class<? extends ParameterChoice> dynamicType = (Class<? extends ParameterChoice>) type;
			newValue = loadChoice(dynamicType,objectNode);

		} else if (ParameterSerializable.class.isAssignableFrom(type)) {
			ParameterSerializable inner = (ParameterSerializable) currentValue;
			if (inner == null)
				try {
					inner = (ParameterSerializable) type.newInstance();
				} catch (InstantiationException ex) {
					throw new RuntimeException(ex);
				}
			newValue = load(inner, objectNode);

		} else if (type.isArray()) {
			newValue = loadArray(type, annotationSource, currentValue, objectNode);

		} else if (type.isAssignableFrom(Map.class)) {
			newValue = loadMap(annotationSource, currentValue, objectNode);

		} else {
			throw new IllegalArgumentException("Unknown field type");
		}
		return newValue;
	}

	private void saveObject(Class<?> type, AnnotatedElement annotationSource, Object value, Element tag, Document doc) {
		if (isPrimitive(type) || type.isEnum()) {
			tag.setTextContent(value.toString());

		} else if (ParameterChoice.class.isAssignableFrom(type)) {
			ParameterChoice inner = (ParameterChoice) value;
			saveChoice(inner, tag);

		} else if (ParameterSerializable.class.isAssignableFrom(type)) {
			ParameterSerializable inner = (ParameterSerializable) value;
			save(inner, tag, doc);

		} else if (type.isArray()) {
			saveArray(type, annotationSource, value, tag, doc);

		} else if (type.isAssignableFrom(Map.class)) {
			saveMap(annotationSource, value, tag, doc);

		} else {
			throw new IllegalArgumentException("Unknown field type");
		}
	}

	private Object loadArray(Class<?> arrayType, AnnotatedElement arrayAnnotations,
			Object currentArray, Node arrayNode)
					throws IllegalArgumentException, IllegalAccessException {
		return loadArray(arrayType, arrayAnnotations, currentArray, 0, arrayNode);
	}

	private Object loadArray(Class<?> arrayType, AnnotatedElement arrayAnnotations,
			Object currentArray, int depth, Node arrayNode)
					throws IllegalArgumentException, IllegalAccessException {

		Class<?> componentType = arrayType.getComponentType();
		if (!canSerializeArray(componentType))
			throw new IllegalArgumentException("Unknown array type");

		ConfList listOptions = arrayAnnotations.getAnnotation(ConfList.class);
		if (listOptions == null)
			throw new IllegalArgumentException("Config lists must be tagged @ConfList");

		List<Object> result = new ArrayList<Object>();

		NodeList children = arrayNode.getChildNodes();
		int arrayIndex = 0;
		for (int i = 0; i < children.getLength(); i++) {
			Node itemNode = children.item(i);

			if (!itemNode.getNodeName().startsWith(listOptions.indexName()[depth]))
				continue;

			Object currentItem = null;
			if (arrayIndex < Array.getLength(currentArray))
				currentItem = Array.get(currentArray, arrayIndex);

			Object newItem = currentItem;
			if (componentType.isArray())
				newItem = loadArray(componentType, arrayAnnotations, currentItem, depth + 1, itemNode);
			else
				newItem = loadObject(componentType, arrayAnnotations, currentItem, itemNode);

			result.add(newItem);
			arrayIndex++;
		}

		Object newArray = Array.newInstance(componentType, result.size());
		for (int i = 0; i < result.size(); i++) {
			Array.set(newArray, i, result.get(i));
		}

		return newArray;
	}

	private void saveArray(Class<?> arrayType, AnnotatedElement arrayAnnotations, Object array,
			Element tag, Document doc) {
		saveArray(arrayType, arrayAnnotations, array, 0, tag, doc);
	}

	private void saveArray(Class<?> arrayType, AnnotatedElement arrayAnnotations, Object array, int depth,
			Element tag, Document doc) {

		Class<?> componentType = arrayType.getComponentType();
		if (!canSerializeArray(componentType))
			throw new IllegalArgumentException("Unknown array type");

		ConfList listOptions = arrayAnnotations.getAnnotation(ConfList.class);
		if (listOptions == null)
			throw new IllegalArgumentException("Config lists must be tagged @ConfList");

		for(int i = 0; i < Array.getLength(array); i++) {
			int outputIndex = listOptions.startAtOne() ? i + 1 : i;
			Element itemTag = doc.createElement(listOptions.indexName()[depth]);
			itemTag.setAttribute("id", Integer.toString(outputIndex));

			Object item = Array.get(array, i);

			if (componentType.isArray())
				saveArray(componentType, arrayAnnotations, item, depth + 1, itemTag, doc);
			else
				saveObject(componentType, arrayAnnotations, item, itemTag, doc);

			tag.appendChild(itemTag);
		}
	}

	protected boolean canSerializeArray(Class<?> componentType) {
		for (Class<?> ct = componentType; ; ct = ct.getComponentType()) {
			if (canSerialize(ct))
				return true;
			else if (!ct.isArray())
				return false;
		}
	}

	private Object loadMap(AnnotatedElement mapAnnotations, Object currentMap, Node mapNode)
			throws IllegalArgumentException, IllegalAccessException {

		ConfMap mapOptions = mapAnnotations.getAnnotation(ConfMap.class);
		if (mapOptions == null)
			throw new IllegalArgumentException("Config maps must be tagged @ConfList");

		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>) currentMap;

		if (result == null)
			result = new LinkedHashMap<String, Object>();

		NodeList children = mapNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node itemNode = children.item(i);

			if (!itemNode.getNodeName().equals(mapOptions.entryName()))
				continue;

			String key = itemNode.getAttributes().getNamedItem(mapOptions.keyName()).getTextContent();

			Object currentItem = result.get(key);

			Object newItem = loadObject(mapOptions.valueClass(), mapAnnotations, currentItem, itemNode);

			result.put(key, newItem);
		}

		return result;
	}

	private void saveMap(AnnotatedElement mapAnnotations, Object currentMap, Element tag,
			Document doc) {

		ConfMap mapOptions = mapAnnotations.getAnnotation(ConfMap.class);
		if (mapOptions == null)
			throw new IllegalArgumentException("Config maps must be tagged @ConfList");

		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>) currentMap;

		for(Entry<String, Object>i : result.entrySet()) {
			Element itemTag = doc.createElement(mapOptions.entryName());
			itemTag.setAttribute(mapOptions.keyName(), i.getKey());

			saveObject(mapOptions.valueClass(), mapAnnotations, i.getValue(), itemTag, doc);

			tag.appendChild(itemTag);
		}
	}



	protected boolean isPrimitive(Class<?> t) {
		return t.isPrimitive() || t.equals(String.class);
	}

	protected boolean canSerialize(Class<?> T) {
		return isPrimitive(T) ||
				ParameterSerializable.class.isAssignableFrom(T) ||
				ParameterChoice.class.isAssignableFrom(T);
	}

	protected Object loadEnum(Class<?> type, String text) {
		try {
			// Simple enums
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Enum<?> newValue = Enum.valueOf((Class<? extends Enum>)type, text);
			return newValue;
		} catch(IllegalArgumentException ex) {
			// nothing
		}

		try {
			// Enums with custom names, requires defining static fromString method
			Object newValue = type
					.getMethod("fromString", String.class)
					.invoke(null, text);
			return newValue;
		} catch (IllegalAccessException | InvocationTargetException |
				NoSuchMethodException | SecurityException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected ParameterChoice loadChoice(Class<? extends ParameterChoice> type, Node node) {
		String identifier = node.getTextContent();
		for (ParameterChoice x : parts.getChoices(type)) {
			if (identifier == null && x.getIdentifier() == null)
				return x;
			else if (identifier != null && identifier.equals(x.getIdentifier()))
				return x;
		}
		throw new IllegalArgumentException("Could not load ParameterChoice" + node.getNodeName());
	}


	protected void saveChoice(ParameterChoice obj, Element node) {
		node.setTextContent(obj.getIdentifier());
	}
}
