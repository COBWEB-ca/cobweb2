/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfMap;
import org.cobweb.io.ParameterChoice;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.util.ReflectionUtil;

/**
 * Table model that binds to CobwebParam object exposed fields
 * For an example, see the Resources and Agents tabs
 */
public class ConfigTableModel extends AbstractTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -8556152150949927964L;

	private final String prefix;

	/**
	 * Creates table model for array of CobwebParam
	 * @param data CobwebParam array to display as columns
	 * @param prefix Prefix for the column names
	 */
	public ConfigTableModel(Object[] data, String prefix) {
		super();
		this.data = data;
		this.prefix = prefix;
		columns = data.length;

		Object d = data[0];
		Class<?> c = d.getClass();
		for (Field f : c.getFields()) {
			ConfDisplayName display = f.getAnnotation(ConfDisplayName.class);
			if (display == null)
				continue;

			if (f.getType().isArray()) {
				int len;
				try {
					len = Array.getLength(f.get(data[0]));
					for (int i = 0; i < len; i++){
						fields.add(new MyArrayField(f, i));
						rowNames.add(display.value() + " " + (i + 1));
					}
				} catch (IllegalAccessException ex) {
					throw new IllegalArgumentException("Unable to access field " + f.getName(), ex);
				}
			} else if (Map.class.isAssignableFrom(f.getType())) {
				try {
					Map<?, ?> col = (Map<?, ?>) f.get(data[0]);
					for (Object k : col.keySet()) {
						fields.add(new MyMapField(f, k));
						rowNames.add(k.toString());
					}

				} catch (IllegalArgumentException ex) {
					throw new RuntimeException(ex);
				} catch (IllegalAccessException ex) {
					throw new RuntimeException(ex);
				}
			} else {
				fields.add(new MyField(f));
				rowNames.add(display.value());
			}
		}
	}

	private class MyMapField extends MyField {
		private Object key;

		private MyMapField(Field f, Object key) {
			super(f);
			this.key = key;

		}

		@Override
		public Object getValue(Object obj) {
			Map<Object, Object> map = getMap(obj);
			return map.get(key);
		}

		@Override
		public void setValue(Object cobwebParam, Object value) {
			Map<Object, Object> map = getMap(cobwebParam);
			map.put(key, value);
		}

		protected Map<Object, Object> getMap(Object obj) {
			@SuppressWarnings("unchecked")
			Map<Object, Object> map = (Map<Object, Object>) super.getValue(obj);
			return map;
		}

		@Override
		public String toString() {
			return super.toString() + "[" + key + "]";
		}

		@Override
		public Class<?> getDeclaredClass() {
			return field.getAnnotation(ConfMap.class).valueClass();
		}
	}

	private class MyArrayField extends MyField {
		protected int index;

		private MyArrayField(Field f, int i) {
			super(f);
			this.index = i;
		}

		@Override
		public String toString() {
			return super.toString() + "[" + index + "]";
		}

		@Override
		public Object getValue(Object param) {
			Object value = null;
			value = Array.get(super.getValue(param), index);
			return value;
		}

		@Override
		public void setValue(Object cobwebParam, Object value) {
			try {
				Object array = super.getValue(cobwebParam);
				fromBoxedToElement(array, index, value);
			} catch (IllegalArgumentException ex) {
				return;
				//throw new UserInputException("Invalid Value");
			}
		}

		@Override
		public Class<?> getDeclaredClass() {
			return super.getDeclaredClass().getComponentType();
		}
	}

	private class MyField {
		private MyField(Field f) {
			field = f;
		}

		protected Field field;

		@Override
		public String toString() {
			return field.toString();
		}

		public Object getValue(Object param) {
			Object value = null;
			try {
				value = this.field.get(param);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException("This field seems to be broken: " + this.toString() , ex);
			}
			return value;
		}
		public void setValue(Object cobwebParam, Object value) {
			Field f = this.field;
			try {
				fromBoxedToField(cobwebParam, f, value);
			} catch (IllegalAccessException ex) {
				throw new IllegalArgumentException("Tagged field is not public: " + f.getName(), ex);
			} catch (IllegalArgumentException ex) {
				return;
				//throw new UserInputException("Invalid Value");
			}
		}

		public Class<?> getDeclaredClass() {
			return field.getType();
		}
	}

	private Object[] data;

	private List<MyField> fields = new ArrayList<MyField>();

	private List<String> rowNames = new ArrayList<String>();

	public ConfigTableModel(Object data, String prefix) {
		this(new Object[] { data }, prefix);
	}

	public ChoiceCatalog choiceCatalog = null;

	private int columns;

	@Override
	public int getColumnCount() {
		return columns + 1;
	}

	@Override
	public int getRowCount() {
		return fields.size();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex > 0;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0)
			return rowNames.get(row);

		MyField mf = fields.get(row);
		return mf.getValue(data[col-1]);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col == 0)
			return;

		MyField mf = fields.get(row);

		mf.setValue(data[col-1], value);
	}

	private static final void fromBoxedToElement(Object array, int index, Object value) {
		if (value instanceof String && !array.getClass().getComponentType().equals(String.class)) {
			String v = (String) value;
			try {
				parseStringToElement(array, index, v);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException(ex);
			}
		} else {
			Array.set(array, index, value);
		}
	}

	private static final void fromBoxedToField(Object o, Field f, Object value) throws IllegalAccessException {
		if (value instanceof String && !f.getType().equals(String.class)) {
			try {
				parseStringToField(o, f, (String) value);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException(ex);
			}
		} else {
			f.set(o, value);
		}
	}

	private static final void parseStringToField(Object object, Field field, String value) throws IllegalAccessException {
		Class<?> t = field.getType();
		Object val = ReflectionUtil.stringToBoxed(t, value);
		field.set(object, val);
	}

	private static final void parseStringToElement(Object array, int index, String value) {
		Class<?> destType = array.getClass().getComponentType();
		Object val = ReflectionUtil.stringToBoxed(destType, value);
		Array.set(array, index, val);
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0)
			return "";

		if (columns > 1)
			return prefix + " " + column;

		return prefix;
	}

	@SuppressWarnings("unchecked")
	public <T extends ParameterChoice> Set<T> getRowOptions(int row) {
		if (choiceCatalog == null)
			throw new IllegalArgumentException("ConfigTableModel needs choiceCatalog for this row");

		Class<T> clazz =(Class<T>) fields.get(row).getDeclaredClass();
		Set<T> res = choiceCatalog.getChoices(clazz);
		return res;
	}
}
