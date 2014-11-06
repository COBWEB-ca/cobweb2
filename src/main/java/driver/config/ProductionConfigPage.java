package driver.config;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import cwcore.complexParams.ProductionParams;


public class ProductionConfigPage implements ConfigPage {
	JPanel prodPanel;
	private MixedValueJTable prodParamTable;

	public ProductionConfigPage(ProductionParams[] params) {
		prodPanel = new JPanel(new BorderLayout());

		prodParamTable = new MixedValueJTable();
		prodParamTable.setModel(new ConfigTableModel(params, "Agent "));
		TableColumnModel agParamColModel = prodParamTable.getColumnModel();
		agParamColModel.getColumn(0).setPreferredWidth(200);
		JScrollPane sp = new JScrollPane(prodParamTable);
		GUI.makeGroupPanel(sp, "Resource Production");

		GUI.colorHeaders(prodParamTable, true);

		prodPanel.add(sp);
	}

	@Override
	public JPanel getPanel() {
		return prodPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		GUI.updateTable(prodParamTable);
	}
}
