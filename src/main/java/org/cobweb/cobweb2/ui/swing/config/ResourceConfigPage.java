package org.cobweb.cobweb2.ui.swing.config;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import org.cobweb.cobweb2.core.params.ComplexFoodParams;
import org.cobweb.swingutil.ColorLookup;


public class ResourceConfigPage implements ConfigPage {

	private MixedValueJTable resourceParamTable;

	private JPanel myPanel = new JPanel();

	public ResourceConfigPage(ComplexFoodParams[] params, ColorLookup agentColors) {
		resourceParamTable = new MixedValueJTable(new ConfigTableModel(params, "Food "));

		TableColumnModel colModel = resourceParamTable.getColumnModel();
		colModel.getColumn(0).setPreferredWidth(120);

		Util.colorHeaders(resourceParamTable, true, agentColors);

		JScrollPane resourceScroll = new JScrollPane(resourceParamTable);
		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.X_AXIS));
		Util.makeGroupPanel(myPanel, "Resource Parameters");
		myPanel.add(resourceScroll);
	}

	@Override
	public JPanel getPanel() {
		return myPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		Util.updateTable(resourceParamTable);
	}

}
