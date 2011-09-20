package driver.config;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;

import cwcore.complexParams.ProductionParams;


public class ProductionConfigPage implements ConfigPage {
	JPanel prodPanel;
	private MixedValueJTable prodParamTable;

	public ProductionConfigPage(ProductionParams[] params) {
		prodPanel = new JPanel();
		prodPanel.setLayout(new BoxLayout(prodPanel, BoxLayout.X_AXIS));
		GUI.makeGroupPanel(prodPanel, "Production parameters");

		prodParamTable = new MixedValueJTable();
		prodParamTable.setModel(new ConfigTableModel(params, "Production "));
		TableColumnModel agParamColModel = prodParamTable.getColumnModel();
		agParamColModel.getColumn(0).setPreferredWidth(200);

		GUI.colorHeaders(prodParamTable, true);

		prodPanel.add(prodParamTable);
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
