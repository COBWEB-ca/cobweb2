package org.cobweb.cobweb2.ui.config;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;

import org.cobweb.io.ConfDisplayName;

/**
 * PropertyAccessor that gets/sets object fields.
 */
public class FieldPropertyAccessor extends PropertyAccessorBase {
	/**
	 * Creates accessor for object field
	 * @param f field to use
	 */
	public FieldPropertyAccessor(Field f) {
		this(null, f);
	}

	/**
	 * Creates a nested accessor, for example obj.a.b can be accessed with
	 * new FieldPropertyAccessor(new FieldPropertyAccessor(aField), bField);
	 * @param parent parent accessor
	 * @param f field to use
	 */
	public FieldPropertyAccessor(PropertyAccessor parent, Field f) {
		super(parent);
		field = f;
	}

	protected Field field;

	@Override
	public String thisGetName() {
		ConfDisplayName nameAnnotation = getAnnotationSource().getAnnotation(ConfDisplayName.class);
		String name = nameAnnotation != null ? nameAnnotation.value() : field.getName();
		return name;
	}

	@Override
	public Object thisGetValue(Object object) {
		Object value = null;
		try {
			value = this.field.get(object);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("This field seems to be broken: " + this.toString() , ex);
		}
		return value;
	}

	@Override
	public void thisSetValue(Object object, Object value) {
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

	@Override
	public String thisToString() {
		return "." + field.getName();
	}

	@Override
	public AnnotatedElement getAnnotationSource() {
		return field;
	}
}
