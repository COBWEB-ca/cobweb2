package org.cobweb.cobweb2.ui.swing.config;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;

import org.cobweb.swingutil.TypeColorEnumeration;


public class Util {

	static void colorHeaders(JTable ft, boolean skipFirst) {
		TypeColorEnumeration tc = TypeColorEnumeration.getInstance();

		int firstCol = skipFirst ? 1 : 0;

		for (int col = firstCol; col < ft.getColumnCount(); col++) {
			DefaultTableCellRenderer r = new DefaultTableCellRenderer();
			r.setBackground(tc.getColor(col - firstCol, 0));
			ft.getColumnModel().getColumn(col).setHeaderRenderer(r);
			LookAndFeel.installBorder(ft.getTableHeader(), "TableHeader.cellBorder");
		}
	}

	static void makeGroupPanel(JComponent target, String title) {
		target.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.blue), title));
	}

	static void updateTable(JTable table) {
		int row = table.getEditingRow();
		int col = table.getEditingColumn();
		if (table.isEditing()) {
			table.getCellEditor(row, col).stopCellEditing();
		}
	}

}
