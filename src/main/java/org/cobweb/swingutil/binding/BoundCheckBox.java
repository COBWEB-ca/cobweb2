package org.cobweb.swingutil.binding;

import javax.swing.JCheckBox;

import org.cobweb.cobweb2.ui.config.PropertyAccessor;

public class BoundCheckBox extends JCheckBox implements FieldBoundComponent {

	private class BoundButtonModel extends ToggleButtonModel {
		private static final long serialVersionUID = -5478297230476196970L;

		@Override
		public boolean isSelected() {
			return (boolean) field.get(obj);
		}

		@Override
		public void setSelected(boolean b) {
			field.set(obj, b);
		}
	}

	private static final long serialVersionUID = -4621056922233460755L;
	private final Object obj;
	private final PropertyAccessor field;

	private final String label;

	public BoundCheckBox(Object obj, PropertyAccessor accessor) {
		this.obj = obj;
		this.field = accessor;
		if (!field.getType().equals(boolean.class) && !field.getType().equals(Boolean.class))
			throw new IllegalArgumentException("Accessor type must be boolean");

		setModel(new BoundButtonModel());
		this.label = accessor.getName();
	}

	@Override
	public String getLabelText() {
		return label;
	}
}
