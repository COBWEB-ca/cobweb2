/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.cobweb.cobweb2.plugins.disease.DiseaseParams;
import org.cobweb.io.ChoiceCatalog;
import org.cobweb.swingutil.ColorLookup;

/**
 * Configuration page for Disease
 */
public class DiseaseConfigPage implements ConfigPage {

	private JPanel myPanel;

	private JTable confTable;

	/**
	 * Sets up the disease configuration page from the simulation parameters
	 *
	 * @param params the agent specific disease parameters
	 */
	public DiseaseConfigPage(DiseaseParams[] params, ChoiceCatalog choiceCatalog, ColorLookup agentColors) {
		ConfigTableModel ctm = new ConfigTableModel(params, "Agent");
		ctm.choiceCatalog = choiceCatalog;
		confTable = new MixedValueJTable(ctm);
		confTable.setRowHeight(20);
		JScrollPane sp = new JScrollPane(confTable);

		Util.makeGroupPanel(sp, "Disease Parameters");
		Util.colorHeaders(confTable, true, agentColors);
		confTable.getColumnModel().getColumn(0).setPreferredWidth(150);

		myPanel = new JPanel(new BorderLayout());
		myPanel.add(sp);

	}

	@Override
	public JPanel getPanel() {
		return myPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		Util.updateTable(confTable);
	}

}
