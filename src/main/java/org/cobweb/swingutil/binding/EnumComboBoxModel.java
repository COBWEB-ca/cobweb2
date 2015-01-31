/**
 *
 */
package org.cobweb.swingutil.binding;

import javax.swing.DefaultComboBoxModel;

import org.cobweb.cobweb2.ui.config.PropertyAccessor;

public class EnumComboBoxModel<T extends Enum<T>> extends DefaultComboBoxModel<T> {
	private static final long serialVersionUID = -9190442597939410887L;

	private T[] values;
	private PropertyAccessor field;
	private Object obj;

	@SuppressWarnings("unchecked")
	public EnumComboBoxModel(Object obj, PropertyAccessor accessor) {
		this.field = accessor;
		Class<?> type = field.getType();
		if (!type.isEnum())
			throw new IllegalArgumentException("Field must be an enum");

		this.obj = obj;
		values = (T[]) type.getEnumConstants();
	}

	@Override
	public Object getSelectedItem() {
		return field.get(obj);
	}

	@Override
	public void setSelectedItem(Object arg0) {
		field.set(obj, arg0);
	}

	@Override
	public T getElementAt(int arg0) {
		return values[arg0];
	}

	@Override
	public int getSize() {
		return values.length;
	}

}