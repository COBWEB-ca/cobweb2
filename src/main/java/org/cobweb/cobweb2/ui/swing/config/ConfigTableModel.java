/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.cobweb.cobweb2.io.CobwebParam;
import org.cobweb.cobweb2.io.NamedParam;
import org.cobweb.io.ConfDisplayName;
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
	public ConfigTableModel(CobwebParam[] data, String prefix) {
		super();
		this.data = data;
		this.prefix = prefix;
		columns = data.length;

		CobwebParam d = data[0];
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
			} else if (List.class.isAssignableFrom(f.getType())) {
				try {
					@SuppressWarnings("unchecked")
					List<NamedParam> col = (List<NamedParam>) f.get(data[0]);
					for (int i = 0; i < col.size(); i++) {
						NamedParam param = col.get(i);
						fields.add(new MyNamedField(f, i));
						rowNames.add(param.getName());
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

	protected class MyNamedField extends MyArrayField {

		private MyNamedField(Field f, int i) {
			super(f, i);
		}

		@Override
		public String toString() {
			return super.toString() + "(list)";
		}

		@Override
		public Object getValue(CobwebParam param) {
			Object value = null;
			try {
				@SuppressWarnings("unchecked")
				List<NamedParam> list = (List<NamedParam>) this.field.get(param);
				NamedParam p = list.get(index);
				value = p.getField().get(p);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException("This field seems to be broken: " + this.toString() , ex);
			}
			return value;
		}

		@Override
		public void setValue(CobwebParam cobwebParam, Object value) {
			try {
				@SuppressWarnings("unchecked")
				List<NamedParam> list = (List<NamedParam>) this.field.get(cobwebParam);
				fromBoxedToField(list.get(index), list.get(index).getField(), value);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException("This field seems to be broken: " + this.toString() , ex);
			}
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
			return field.toString() + "[" + index + "]";
		}

		@Override
		public Object getValue(CobwebParam param) {
			Object value = null;
			try {
				value = Array.get(this.field.get(param), index);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException("This field seems to be broken: " + this.toString() , ex);
			}
			return value;
		}

		@Override
		public void setValue(CobwebParam cobwebParam, Object value) {
			try {
				Object array = field.get(cobwebParam);
				fromBoxedToElement(array, index, value);
			} catch (IllegalAccessException ex) {
				throw new IllegalArgumentException("Tagged field is not public: " + field.getName(), ex);
			} catch (IllegalArgumentException ex) {
				return;
				//throw new UserInputException("Invalid Value");
			}

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

		public Object getValue(CobwebParam param) {
			Object value = null;
			try {
				value = this.field.get(param);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException("This field seems to be broken: " + this.toString() , ex);
			}
			return value;
		}
		public void setValue(CobwebParam cobwebParam, Object value) {
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
	}

	private CobwebParam[] data;

	private List<MyField> fields = new ArrayList<MyField>();

	private List<String> rowNames = new ArrayList<String>();

	public ConfigTableModel(CobwebParam data, String prefix) {
		this(new CobwebParam[] { data }, prefix);
	}

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
}
