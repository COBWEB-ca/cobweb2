package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import org.cobweb.io.ChoiceCatalog;
import org.cobweb.io.ParameterSerializable;
import org.cobweb.swingutil.ColorLookup;


public class TableConfigPage<T extends ParameterSerializable> implements ConfigPage {

	private final JPanel panel;
	private final MixedValueJTable paramTable;

	public TableConfigPage(T[] params, String name, ColorLookup agentColors) {
		this(params, name, null, agentColors);
	}

	public TableConfigPage(T[] params, String name, ColorLookup agentColors, String colPrefix) {
		this(params, name, null, agentColors, colPrefix);
	}

	public TableConfigPage(T[] params, String name, ChoiceCatalog catalog, ColorLookup agentColors) {
		this(params, name, catalog, agentColors, "Agent");
	}

	public TableConfigPage(T[] params, String name, ChoiceCatalog catalog, ColorLookup agentColors, String colPrefix) {
		panel = new JPanel(new BorderLayout());

		ConfigTableModel model = new ConfigTableModel(params, colPrefix + " ");
		model.choiceCatalog = catalog;

		paramTable = new MixedValueJTable(model);
		TableColumnModel agParamColModel = paramTable.getColumnModel();
		agParamColModel.getColumn(0).setPreferredWidth(200);
		JScrollPane sp = new JScrollPane(paramTable);
		Util.makeGroupPanel(sp, name);

		Util.colorHeaders(paramTable, true, agentColors);

		panel.add(sp);
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		Util.updateTable(paramTable);
	}
}
