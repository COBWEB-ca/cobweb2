package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import org.cobweb.cobweb2.production.ProductionParams;
import org.cobweb.swingutil.ColorLookup;


public class ProductionConfigPage implements ConfigPage {
	private JPanel prodPanel;
	private MixedValueJTable prodParamTable;

	public ProductionConfigPage(ProductionParams[] params, ColorLookup agentColors) {
		prodPanel = new JPanel(new BorderLayout());

		prodParamTable = new MixedValueJTable(new ConfigTableModel(params, "Agent "));
		TableColumnModel agParamColModel = prodParamTable.getColumnModel();
		agParamColModel.getColumn(0).setPreferredWidth(200);
		JScrollPane sp = new JScrollPane(prodParamTable);
		Util.makeGroupPanel(sp, "Resource Production");

		Util.colorHeaders(prodParamTable, true, agentColors);

		prodPanel.add(sp);
	}

	@Override
	public JPanel getPanel() {
		return prodPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		Util.updateTable(prodParamTable);
	}
}
