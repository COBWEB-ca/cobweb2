package org.cobweb.swingutil.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;

import javax.swing.JFormattedTextField;

import org.cobweb.cobweb2.ui.config.PropertyAccessor;

public class BoundJFormattedTextField extends JFormattedTextField implements FieldBoundComponent, PropertyChangeListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 6928975337583519338L;

	private final Object obj;
	private final PropertyAccessor field;

	public BoundJFormattedTextField(Object obj, PropertyAccessor accessor, Format format) {
		super(format);
		this.obj = obj;
		this.field = accessor;

		// Set current value
		this.setValue(field.get(this.obj));
		this.addPropertyChangeListener("value", this);
	}

	@Override
	@SuppressWarnings("boxing")
	public void propertyChange(PropertyChangeEvent evt) {
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
	}

	@Override
	public String getLabelText() {
		return field.getName();
	}

}
