/**
 * 
 */
package driver.config;

import java.lang.reflect.Field;

import javax.swing.DefaultComboBoxModel;

class EnumComboBoxModel extends DefaultComboBoxModel {
	private static final long serialVersionUID = -9190442597939410887L;

	Object[] values;
	Field field;
	Object obj;
	
	public EnumComboBoxModel(Object obj, String fieldName) {
		try {
			this.field = obj.getClass().getField(fieldName);
		} catch (NoSuchFieldException ex) {
			throw new RuntimeException(ex);
		}
		Class<?> type = field.getType();
		if (!type.isEnum())
			throw new IllegalArgumentException("Field must be an enum");
		
		this.obj = obj;
		values = type.getEnumConstants();
	}
	
	@Override
	public Object getSelectedItem() {
		try {
			return field.get(obj);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void setSelectedItem(Object arg0) {
		try {
			field.set(obj, arg0);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Object getElementAt(int arg0) {
		return values[arg0];
	}

	@Override
	public int getSize() {
		return values.length;
	}

}