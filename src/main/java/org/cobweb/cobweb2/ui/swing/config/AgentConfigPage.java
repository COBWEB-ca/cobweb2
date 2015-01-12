package org.cobweb.cobweb2.ui.swing.config;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import org.cobweb.cobweb2.core.params.ComplexAgentParams;
import org.cobweb.swingutil.ColorLookup;


public class AgentConfigPage implements ConfigPage {
	private final JPanel agentPanel;
	private final MixedValueJTable agentParamTable;

	public AgentConfigPage(ComplexAgentParams[] params, ColorLookup agentColors) {
		agentPanel = new JPanel();
		agentPanel.setLayout(new BoxLayout(agentPanel, BoxLayout.X_AXIS));
		Util.makeGroupPanel(agentPanel, "Agent Parameters");

		agentParamTable = new MixedValueJTable();
		agentParamTable.setModel(new ConfigTableModel(params, "Agent "));

		TableColumnModel agParamColModel = agentParamTable.getColumnModel();
		// Get the column at index pColumn, and set its preferred width.
		agParamColModel.getColumn(0).setPreferredWidth(200);


		Util.colorHeaders(agentParamTable, true, agentColors);
		JScrollPane agentScroll = new JScrollPane(agentParamTable);
		// Add the scroll pane to this panel.
		agentPanel.add(agentScroll);
	}

	@Override
	public JPanel getPanel() {
		return agentPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {
		Util.updateTable(agentParamTable);
	}

}
