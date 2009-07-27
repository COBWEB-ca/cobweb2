/**
 *
 */
package driver.config;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

class MixedValueJTable extends JTable {

	private static final long serialVersionUID = -9106510371599896107L;

	public MixedValueJTable() {
		super();
		this.getTableHeader().setReorderingAllowed(false);
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		TableColumn tableColumn = getColumnModel().getColumn(column);
		TableCellEditor editor = tableColumn.getCellEditor();
		if (editor == null && getValueAt(row, column) != null) {
			editor = getDefaultEditor(getValueAt(row, column).getClass());
		}
		if (editor == null) {
			editor = getDefaultEditor(getColumnClass(column));
		}
		return editor;
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		TableColumn tableColumn = getColumnModel().getColumn(column);
		TableCellRenderer renderer = tableColumn.getCellRenderer();
		if (renderer == null && getValueAt(row, column) != null) {
			renderer = getDefaultRenderer(getValueAt(row, column).getClass());
		}
		if (renderer == null) {
			renderer = getDefaultRenderer(getColumnClass(column));
		}
		return renderer;

	}
}