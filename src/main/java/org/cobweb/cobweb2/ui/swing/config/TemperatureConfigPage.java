package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.cobweb.cobweb2.abiotic.TemperatureParams;
import org.cobweb.swingutil.ColorLookup;

public class TemperatureConfigPage implements ConfigPage {

	private JPanel myPanel;
	private JTable agentTable;
	private JTable bandsConf;

	public TemperatureConfigPage(TemperatureParams params, ColorLookup agentColors) {

		ConfigTableModel ctm = new ConfigTableModel(params, "Abiotic Factor");
		bandsConf = new MixedValueJTable(ctm);
		JScrollPane sp = new JScrollPane(bandsConf);
		sp.setPreferredSize(new Dimension(200, 200));
		Util.makeGroupPanel(sp, "Environment Bands");

		ConfigTableModel agentConf = new ConfigTableModel(params.agentParams, "Agent");
		agentTable = new MixedValueJTable(agentConf);
		JScrollPane sp2 = new JScrollPane(agentTable);
		Util.makeGroupPanel(sp2, "Agent Preferences");
		agentTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		Util.colorHeaders(agentTable, true, agentColors);

		myPanel = new JPanel(new BorderLayout());
		myPanel.add(sp, BorderLayout.NORTH);
		myPanel.add(sp2);

	}

	@Override
	public JPanel getPanel() {
		return myPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		Util.updateTable(this.bandsConf);
		Util.updateTable(agentTable);
	}

}
