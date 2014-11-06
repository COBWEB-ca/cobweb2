package driver.config;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import temperature.TemperatureParams;

public class TemperatureConfigPage implements ConfigPage {

	private JPanel myPanel;
	private JTable agentTable;
	private JTable bandsConf;

	public TemperatureConfigPage(TemperatureParams params) {

		ConfigTableModel ctm = new ConfigTableModel(params, "Temperature");
		bandsConf = new MixedValueJTable();
		bandsConf.setModel(ctm);
		JScrollPane sp = new JScrollPane(bandsConf);
		sp.setPreferredSize(new Dimension(200, 200));
		GUI.makeGroupPanel(sp, "Environment Temperature");

		ConfigTableModel agentConf = new ConfigTableModel(params.agentParams, "Agent");
		agentTable = new MixedValueJTable();
		agentTable.setModel(agentConf);
		JScrollPane sp2 = new JScrollPane(agentTable);
		GUI.makeGroupPanel(sp2, "Agent Preferences");
		agentTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		GUI.colorHeaders(agentTable, true);

		myPanel = new JPanel(new BorderLayout());
		myPanel.add(sp, BorderLayout.NORTH);
		myPanel.add(sp2);

	}

	public JPanel getPanel() {
		return myPanel;
	}

	public void validateUI() throws IllegalArgumentException {
		GUI.updateTable(this.bandsConf);
		GUI.updateTable(agentTable);
	}

}
