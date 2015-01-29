package org.cobweb.cobweb2.ui.config;

import java.util.Map;

import org.cobweb.io.ConfDisplayFormat;
import org.cobweb.io.ConfMap;

/**
 * PropertyAccessor that gets/sets map values.
 */
public class MapPropertyAccessor implements PropertyAccessor {
	private final Object key;

	private final FieldPropertyAccessor parent;

	private final String format;

	/**
	 * Creates accessor for value for given key and map returned by parent accessor.
	 * @param parent parent accessor that returns a map
	 * @param key key to access
	 * @param format format for property name. Used as:
	 * <code>String.format(format, parent.getName(), key)</code>
	 * defaults to "%2$s" if null
	 */
	public MapPropertyAccessor(FieldPropertyAccessor parent, Object key, ConfDisplayFormat format) {
		if (!Map.class.isAssignableFrom(parent.getType()))
			throw new IllegalArgumentException("Parent must return a map!");

		this.parent = parent;
		this.key = key;
		if (format == null)
			this.format = "%2$s";
		else
			this.format = format.value();
	}

	@Override
	public Object getValue(Object obj) {
		Map<Object, Object> map = getMap(obj);
		return map.get(key);
	}

	@Override
	public void setValue(Object cobwebParam, Object value) {
		Map<Object, Object> map = getMap(cobwebParam);
		map.put(key, value);
	}

	protected Map<Object, Object> getMap(Object obj) {
		@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>) parent.getValue(obj);
		return map;
	}

	@Override
	public String getName() {
		return String.format(format, parent.getName(), key);
	}

	@Override
	public Class<?> getType() {
		return parent.field.getAnnotation(ConfMap.class).valueClass();
	}
}