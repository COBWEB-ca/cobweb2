package org.cobweb.cobweb2.ui.config;

import java.lang.reflect.Array;

import org.cobweb.io.ConfDisplayFormat;

/**
 * PropertyAccessor that gets/sets an array element.
 */
public class ArrayPropertyAccessor implements PropertyAccessor {
	private final int index;

	private final PropertyAccessor parent;

	private final String format;

	/**
	 * Creates accessor for array element i of array returned by parent accessor.
	 * @param parent parent accessor that returns an array
	 * @param i array index
	 * @param format format for property name. Used as:
	 * <code>String.format(format, parent.getName(), index + 1)</code>
	 * defaults to "%s %d" if null
	 */
	public ArrayPropertyAccessor(PropertyAccessor parent, int i, ConfDisplayFormat format) {
		if (!parent.getType().isArray())
			throw new IllegalArgumentException("Parent must return an array!");

		this.parent = parent;
		this.index = i;
		if (format == null)
			this.format = "%s %d";
		else
			this.format = format.value();
	}

	@Override
	public String getName() {
		return String.format(format, parent.getName(), index + 1);
	}

	@Override
	public Object getValue(Object param) {
		Object value = null;
		value = Array.get(parent.getValue(param), index);
		return value;
	}

	@Override
	public void setValue(Object object, Object value) {
		try {
			Object array = parent.getValue(object);
			Array.set(array, index, value);
		} catch (IllegalArgumentException ex) {
			return;
			//throw new UserInputException("Invalid Value");
		}
	}

	@Override
	public Class<?> getType() {
		return parent.getType().getComponentType();
	}

	@Override
	public String toString() {
		String res = "[" + index + "]";
		if (parent != null)
			res = parent.toString() + res;
		return res;
	}
}
