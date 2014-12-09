package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.cobweb.cobweb2.eventlearning.LearningAgentParams;


public class LearningConfigPage implements ConfigPage {
	private JPanel learningPanel;
	private JTable learnTable;

	public LearningConfigPage(LearningAgentParams[] params) {
		ConfigTableModel ctm = new ConfigTableModel(params, "Agent");
		learnTable = new MixedValueJTable();
		learnTable.setModel(ctm);
		JScrollPane sp = new JScrollPane(learnTable);

		Util.makeGroupPanel(sp, "Agent Learning Parameters");
		Util.colorHeaders(learnTable, true);
		learnTable.getColumnModel().getColumn(0).setPreferredWidth(150);

		learningPanel = new JPanel(new BorderLayout());
		learningPanel.add(sp, BorderLayout.NORTH);
	}

	@Override
	public JPanel getPanel() {
		return learningPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		Util.updateTable(learnTable);
	}
}
