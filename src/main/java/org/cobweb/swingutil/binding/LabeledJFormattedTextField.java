package org.cobweb.swingutil.binding;

import java.lang.reflect.Field;
import java.text.Format;

import javax.swing.JFormattedTextField;

import org.cobweb.cobweb2.io.ConfDisplayName;


public class LabeledJFormattedTextField extends JFormattedTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1794585383726355152L;

	private final String label;

	public LabeledJFormattedTextField(Object obj, String fieldName, Format format) {
		super(format);
		Field field;
		try {
			field = obj.getClass().getField(fieldName);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
		this.label = field.getAnnotation(ConfDisplayName.class).value();
	}

	public String getLabelText() {
		return label;
	}
}
