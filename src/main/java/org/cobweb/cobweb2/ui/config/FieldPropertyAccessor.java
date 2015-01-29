package org.cobweb.cobweb2.ui.config;

import java.lang.reflect.Field;

import org.cobweb.io.ConfDisplayName;

/**
 * PropertyAccessor that gets/sets object fields.
 */
public class FieldPropertyAccessor implements PropertyAccessor {
	private final PropertyAccessor parent;

	/**
	 * Creates accessor for object field
	 * @param f field to use
	 */
	public FieldPropertyAccessor(Field f) {
		this.parent = null;
		field = f;
	}

	/**
	 * Creates a nested accessor, for example obj.a.b can be accessed with
	 * new FieldPropertyAccessor(new FieldPropertyAccessor(aField), bField);
	 * @param parent parent accessor
	 * @param f field to use
	 */
	public FieldPropertyAccessor(PropertyAccessor parent, Field f) {
		this.parent = parent;
		field = f;
	}

	protected Field field;

	@Override
	public String getName() {
		ConfDisplayName nameAnnotation = field.getAnnotation(ConfDisplayName.class);
		String name = nameAnnotation != null ? nameAnnotation.value() : field.getName();

		if (parent != null)
			name = parent.getName() + " " + name;
		return name;
	}

	@Override
	public Object getValue(Object object) {
		Object value = null;

		if (parent != null)
			object = parent.getValue(object);

		try {
			value = this.field.get(object);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("This field seems to be broken: " + this.toString() , ex);
		}
		return value;
	}

	@Override
	public void setValue(Object object, Object value) {
		if (parent != null)
			object = parent.getValue(object);

		try {
			field.set(object, value);
		} catch (IllegalAccessException ex) {
			throw new IllegalArgumentException("Tagged field is not public: " + field.getName(), ex);
		} catch (IllegalArgumentException ex) {
			return;
			//throw new UserInputException("Invalid Value");
		}
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}
}