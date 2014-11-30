/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.cobweb.cobweb2.io.CobwebSelectionParam;

class MixedValueJTable extends JTable {

	public static class CobwebSelectionEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 3458173499957389679L;

		public CobwebSelectionEditor(JComboBox comboBox) {
			super(comboBox);
		}

		@Override
		public void cancelCellEditing() {
			super.cancelCellEditing();
		}

		@Override
		public Object getCellEditorValue() {
			return super.getCellEditorValue();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}

		@Override
		public boolean stopCellEditing() {
			return super.stopCellEditing();
		}

	}

	private final class PerciseDecimalTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = -1919381757017436295L;

		private DecimalFormat formater = new DecimalFormat();

		PerciseDecimalTableCellRenderer(){
			super.setHorizontalAlignment(RIGHT);
		}


		@Override
		public Component getTableCellRendererComponent
		(JTable tbl, Object value, boolean selected, boolean focused, int row, int col){
			value = formater.format(value);
			return super.getTableCellRendererComponent(tbl, value, selected, focused, row, col);
		}
	}

	private static final long serialVersionUID = -9106510371599896107L;

	public MixedValueJTable() {
		super();
		this.getTableHeader().setReorderingAllowed(false);
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		TableColumn tableColumn = getColumnModel().getColumn(column);
		TableCellEditor editor = tableColumn.getCellEditor();
		if (getValueAt(row, column) instanceof  CobwebSelectionParam<?>) {
			CobwebSelectionParam<?> par = (CobwebSelectionParam<?>)getValueAt(row, column);
			editor = new CobwebSelectionEditor(new JComboBox(par.getPossibleValues().toArray()));
		}
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
			if (getValueAt(row, column) instanceof Double ||
					getValueAt(row, column) instanceof Float ){
				renderer = new PerciseDecimalTableCellRenderer();
			} else {
				renderer = getDefaultRenderer(getValueAt(row, column).getClass());
			}
		}
		if (renderer == null) {
			renderer = getDefaultRenderer(getColumnClass(column));
		}
		return renderer;

	}
}