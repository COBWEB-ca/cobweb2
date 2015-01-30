package org.cobweb.cobweb2.ui.config;

import java.util.List;

import org.cobweb.io.ConfDisplayFormat;
import org.cobweb.io.ConfListType;

/**
 * PropertyAccessor that gets/sets a list element.
 */
public class ListPropertyAccessor implements PropertyAccessor {
	private final int index;

	private final FieldPropertyAccessor parent;

	private final String format;

	/**
	 * Creates accessor for list element i of list returned by parent accessor.
	 * @param parent parent accessor that returns an list
	 * @param i list index
	 * @param format format for property name. Used as:
	 * <code>String.format(format, parent.getName(), index + 1)</code>
	 * defaults to "%s %d" if null
	 */
	public ListPropertyAccessor(FieldPropertyAccessor parent, int i, ConfDisplayFormat format) {
		if (!List.class.isAssignableFrom(parent.getType()))
			throw new IllegalArgumentException("Parent must return a List!");

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
	public Object getValue(Object object) {
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) parent.getValue(object);
		Object value = list.get(index);
		return value;
	}

	@Override
	public void setValue(Object object, Object value) {
		try {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) parent.getValue(object);
			list.set(index, value);
		} catch (IllegalArgumentException ex) {
			return;
			//throw new UserInputException("Invalid Value");
		}
	}

	@Override
	public Class<?> getType() {
		return parent.field.getAnnotation(ConfListType.class).value();
	}

	@Override
	public String toString() {
		String res = ".get(" + index + ")";
		if (parent != null)
			res = parent.toString() + res;
		return res;
	}
}
