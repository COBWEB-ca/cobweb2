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

import org.cobweb.cobweb2.ui.config.ArrayPropertyAccessor;
import org.cobweb.cobweb2.ui.config.FieldPropertyAccessor;
import org.cobweb.cobweb2.ui.config.MapPropertyAccessor;
import org.cobweb.cobweb2.ui.config.PropertyAccessor;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.io.ConfDisplayFormat;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ParameterChoice;
import org.cobweb.io.ParameterSerializable;
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
	public ConfigTableModel(ParameterSerializable[] data, String prefix) {
		super();
		this.data = data;
		this.prefix = prefix;
		columns = data.length;

		bindObject(data[0]);
	}

	protected void bindObject(ParameterSerializable d) {
		bindObject(d, null);
	}

	protected void bindObject(ParameterSerializable d, FieldPropertyAccessor parent) {
		Class<?> c = d.getClass();
		for (Field f : c.getFields()) {
			ConfDisplayName display = f.getAnnotation(ConfDisplayName.class);
			ConfDisplayFormat displayFormat = f.getAnnotation(ConfDisplayFormat.class);
			if (display == null && displayFormat == null)
				continue;

			FieldPropertyAccessor fieldAccessor = new FieldPropertyAccessor(parent, f);
			try {

				if (f.getType().isArray()) {
					int len = Array.getLength(f.get(d));
					for (int i = 0; i < len; i++){
						fields.add(new ArrayPropertyAccessor(fieldAccessor, i, displayFormat));
					}
				} else if (Map.class.isAssignableFrom(f.getType())) {
					Map<?, ?> col;
					col = (Map<?, ?>) f.get(d);
					for (Object k : col.keySet()) {
						fields.add(new MapPropertyAccessor(fieldAccessor, k, displayFormat));
					}
				} else if (ParameterSerializable.class.isAssignableFrom(fieldAccessor.getType())) {
					bindObject((ParameterSerializable) f.get(d), fieldAccessor);
				} else {
					fields.add(fieldAccessor);
				}

			} catch (IllegalArgumentException | IllegalAccessException ex) {
				throw new RuntimeException("Could not bind field " + c + "." + f, ex);
			}

		}
	}

	private ParameterSerializable[] data;

	private List<PropertyAccessor> fields = new ArrayList<>();

	public ConfigTableModel(ParameterSerializable data, String prefix) {
		this(new ParameterSerializable[] { data }, prefix);
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
		PropertyAccessor mf = fields.get(row);
		if (col == 0)
			return mf.getName();
		else
			return mf.getValue(data[col-1]);
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col == 0)
			return;

		PropertyAccessor mf = fields.get(row);
		Class<?> declaredClass = mf.getType();

		Object typedValue;
		if (value instanceof String && !declaredClass.equals(String.class)) {
			typedValue = ReflectionUtil.stringToBoxed(declaredClass, (String) value);
		} else {
			typedValue = value;
			assert declaredClass.isAssignableFrom(value.getClass());
		}

		mf.setValue(data[col-1], typedValue);
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

		Class<T> clazz =(Class<T>) fields.get(row).getType();
		Set<T> res = choiceCatalog.getChoices(clazz);
		return res;
	}
}
