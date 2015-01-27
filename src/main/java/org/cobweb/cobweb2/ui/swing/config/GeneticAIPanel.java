/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.impl.ai.GeneticController;
import org.cobweb.cobweb2.impl.ai.GeneticControllerParams;
import org.cobweb.swingutil.ColorLookup;

final class GeneticAIPanel extends SettingsPanel {
	private static final long serialVersionUID = 1139521733160862828L;
	private GeneticControllerParams params;
	private ColorLookup agentColors;

	public GeneticAIPanel(ColorLookup agentColors) {
		this.agentColors = agentColors;
	}

	@Override
	public void bindToParser(SimulationConfig p) {
		if (!(p.controllerParams instanceof GeneticControllerParams)) {
			p.envParams.controllerName = GeneticController.class.getName();
			if (params == null)
				params = new GeneticControllerParams(p);

			p.controllerParams = params;

		} else {
			params = (GeneticControllerParams) p.controllerParams;
		}
		updateBoxes();
	}

	private void updateBoxes() {
		setLayout(new BorderLayout());
		this.removeAll();

		JPanel agentPanel = new JPanel();
		agentPanel.setLayout(new BoxLayout(agentPanel, BoxLayout.X_AXIS));
		Util.makeGroupPanel(agentPanel, "Agent Parameters");

		MixedValueJTable agentParamTable = new MixedValueJTable(
				new ConfigTableModel(params.agentParams, "Agent "));

		TableColumnModel agParamColModel = agentParamTable.getColumnModel();
		// Get the column at index pColumn, and set its preferred width.
		agParamColModel.getColumn(0).setPreferredWidth(200);

		Util.colorHeaders(agentParamTable, true, agentColors);
		JScrollPane agentScroll = new JScrollPane(agentParamTable);
		// Add the scroll pane to this panel.
		agentPanel.add(agentScroll);

		this.add(agentPanel, BorderLayout.CENTER);


	}
}