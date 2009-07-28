/**
 *
 */
package driver.config;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.CobwebParam;
import cobweb.params.ConfDisplayName;
import cobweb.params.ReflectionUtil;

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

			if (!f.getType().isArray()) {

				fields.add(new MyField(f));
				rowNames.add(display.value());
			} else {
				int len;
				try {
					len = Array.getLength(f.get(data[0]));
					for (int i = 0; i < len; i++){
						fields.add(new MyField(f, i));
						rowNames.add(display.value() + " " + (i + 1));
					}
				} catch (IllegalAccessException ex) {
					throw new IllegalArgumentException("Unable to access field " + f.getName(), ex);
				}
			}
		}
	}


	private class MyField {
		public MyField(Field f) {
			field = f;
		}
		public MyField(Field f, int i) {
			field = f;
			index = i;
			array = true;
		}
		private Field field;
		private int index;
		private boolean array = false;

		@Override
		public String toString() {
			if (!array)
				return field.toString();
			else
				return field.toString() + "[" + index + "]";
		}
	}

	private CobwebParam[] data;

	private List<MyField> fields = new ArrayList<MyField>();

	private List<String> rowNames = new ArrayList<String>();

	public ConfigTableModel(AbstractReflectionParams data, String prefix) {
		this(new AbstractReflectionParams[] { data }, prefix);
	}

	private int columns;

	public int getColumnCount() {
		return columns + 1;
	}

	public int getRowCount() {
		return fields.size();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex > 0;
	}

	public Object getValueAt(int row, int col) {
		if (col == 0)
			return rowNames.get(row);

		MyField mf = fields.get(row);
		try {
			if (!mf.array)
				return mf.field.get(data[col-1]);
			else
				return Array.get(mf.field.get(data[col-1]), mf.index);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("This field seems to be broken: " + mf.toString() , ex);
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col == 0)
			return;

		MyField mf = fields.get(row);

		Field f = mf.field;
		try {
			if (!mf.array) {
				CobwebParam o = data[col-1];
				fromBoxedToField(o, f, value);
			} else {
				Object array = f.get(data[col-1]);
				int index = mf.index;
				fromBoxedToElement(array, index, value);
			}
		} catch (IllegalAccessException ex) {
			throw new IllegalArgumentException("Tagged field is not public: " + f.getName(), ex);
		} catch (IllegalArgumentException ex) {
			return;
			//throw new CobwebUserException("Invalid Value");
		}
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
