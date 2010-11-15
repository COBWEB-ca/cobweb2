/**
 *
 */
package driver.config;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.text.DecimalFormat;
import javax.swing.table.DefaultTableCellRenderer;


import cobweb.params.CobwebSelectionParam;

class MixedValueJTable extends JTable {

	private static final long serialVersionUID = -9106510371599896107L;

	private final class PerciseDecimalTableCellRenderer extends DefaultTableCellRenderer {

		private DecimalFormat formater = new DecimalFormat();

		PerciseDecimalTableCellRenderer(){
			super.setHorizontalAlignment(RIGHT);
		}
		
		public Component getTableCellRenderComponent
			(JTable tbl, Object value, boolean selected, boolean focused, int row, int col){
			value = formater.format(value);
			return super.getTableCellRendererComponent(tbl, value, selected, focused, row, col);
		}
	}

	public static class CobwebSelectionEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 3458173499957389679L;

		public CobwebSelectionEditor(JComboBox comboBox) {
			super(comboBox);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
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
		public boolean stopCellEditing() {
			return super.stopCellEditing();
		}
	
	}
	
	CobwebSelectionEditor myEditor = new CobwebSelectionEditor(new JComboBox());
	
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
			}else {
				renderer = getDefaultRenderer(getValueAt(row, column).getClass());
			}
			System.out.println(renderer.getClass().getName());
		}
		if (renderer == null) {
			renderer = getDefaultRenderer(getColumnClass(column));
			System.out.println(getColumnClass(column).getName());
		}
		return renderer;

	}
}