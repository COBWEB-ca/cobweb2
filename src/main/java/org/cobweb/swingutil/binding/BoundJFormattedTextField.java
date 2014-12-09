package org.cobweb.swingutil.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.text.Format;

public class BoundJFormattedTextField extends LabeledJFormattedTextField implements FieldBoundComponent, PropertyChangeListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 6928975337583519338L;

	private final Object obj;
	private final Field field;

	public BoundJFormattedTextField(Object obj, String fieldName, Format format) {
		super(obj, fieldName, format);
		this.obj = obj;
		try {
			this.field = obj.getClass().getField(fieldName);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
		try {
			this.setValue(field.get(obj));
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		this.addPropertyChangeListener("value", this);
	}

	@Override
	@SuppressWarnings("boxing")
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			Object value = evt.getNewValue();
			Object out = null;
			if (field.getType().equals(value.getClass())) {
				out = value;
			} else if (value instanceof Double) {
				Double d = (Double) value;
				if (field.getType().equals(double.class)) {
					out = d;
				} else if (field.getType().equals(float.class)) {
					out = d.floatValue();
				}
			} else if (value instanceof Long) {
				Long l = (Long) value;
				if (field.getType().equals(long.class)) {
					out = l;
				} else if (field.getType().equals(int.class)) {
					out = l.intValue();
				} else if (field.getType().equals(float.class)) {
					out = l.floatValue();
				}
			} else {
				throw new IllegalArgumentException("bad input/output combination: " + value.getClass() + " -> " + field.getType());
			}
			field.set(obj, out);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

}
