package driver.config;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import learning.LearningAgentParams;


public class LearningConfigPage implements ConfigPage {
	private JPanel learningPanel;
	private JTable learnTable;

	public LearningConfigPage(LearningAgentParams[] params) {
		ConfigTableModel ctm = new ConfigTableModel(params, "Agent");
		learnTable = new MixedValueJTable();
		learnTable.setModel(ctm);
		JScrollPane sp = new JScrollPane(learnTable);

		GUI.makeGroupPanel(sp, "Agent Learning Parameters");
		GUI.colorHeaders(learnTable, true);
		learnTable.getColumnModel().getColumn(0).setPreferredWidth(150);

		learningPanel = new JPanel(new BorderLayout());
		learningPanel.add(sp, BorderLayout.NORTH);
	}

	public JPanel getPanel() {
		return learningPanel;
	}

	public void validateUI() throws IllegalArgumentException {
		GUI.updateTable(learnTable);
	}
}
