package driver.config;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import learning.LearningAgentParams;


public class LearningConfigPage implements ConfigPage {
	private JPanel learningPanel;
	private JTable learnTable;

	public LearningConfigPage(LearningAgentParams[] params) {
		ConfigTableModel ctm = new ConfigTableModel(params, "Learning");
		learnTable = new MixedValueJTable();
		learnTable.setModel(ctm);
		JScrollPane sp = new JScrollPane(learnTable);
		sp.setPreferredSize(new Dimension(400, 500));
		GUI.makeGroupPanel(sp, "Agent Learning Parameters");
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
