package org.cobweb.swingutil.binding;

import java.lang.reflect.Field;

import javax.swing.JCheckBox;

import org.cobweb.io.ConfDisplayName;

public class BoundCheckBox extends JCheckBox implements FieldBoundComponent {

	private class BoundButtonModel extends ToggleButtonModel {
		private static final long serialVersionUID = -5478297230476196970L;

		@Override
		public boolean isSelected() {
			try {
				return field.getBoolean(obj);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public void setSelected(boolean b) {
			try {
				field.setBoolean(obj, b);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private static final long serialVersionUID = -4621056922233460755L;
	private final Object obj;
	private final Field field;

	private final String label;

	public BoundCheckBox(Object obj, String fieldName) {
		this.obj = obj;
		try {
			this.field = obj.getClass().getField(fieldName);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
		setModel(new BoundButtonModel());
		this.label = field.getAnnotation(ConfDisplayName.class).value();
	}

	public String getLabelText() {
		return label;
	}
}
