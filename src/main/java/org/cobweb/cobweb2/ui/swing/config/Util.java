package org.cobweb.cobweb2.ui.swing.config;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;

import org.cobweb.swingutil.ColorLookup;


public class Util {

	static void colorHeaders(JTable ft, boolean skipFirst, ColorLookup colorMap) {
		int firstCol = skipFirst ? 1 : 0;

		int count = ft.getColumnCount() - firstCol;
		for (int col = 0; col < count; col++) {
			DefaultTableCellRenderer r = new DefaultTableCellRenderer();
			r.setBackground(colorMap.getColor(col, count));
			ft.getColumnModel().getColumn(col + firstCol).setHeaderRenderer(r);
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
