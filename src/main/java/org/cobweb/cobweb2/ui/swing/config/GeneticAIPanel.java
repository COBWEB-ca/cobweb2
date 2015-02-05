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
			p.setControllerName(GeneticController.class.getName());
			// SimulationConfig sets a blank default, but if the page already has settings, use them
			if (params != null)
				p.controllerParams = params;
		}
		params = (GeneticControllerParams) p.controllerParams;

		updateBoxes();
	}

	private void updateBoxes() {
		setLayout(new BorderLayout());
		this.removeAll();

		JPanel agentPanel = new JPanel();
		agentPanel.setLayout(new BoxLayout(agentPanel, BoxLayout.X_AXIS));
		Util.makeGroupPanel(agentPanel, "Agent Parameters");

		final MixedValueJTable agentParamTable = new MixedValueJTable(
				new ConfigTableModel(params.agentParams, "Agent "));

		TableColumnModel agParamColModel = agentParamTable.getColumnModel();
		// Get the column at index pColumn, and set its preferred width.
		agParamColModel.getColumn(0).setPreferredWidth(200);

		Util.colorHeaders(agentParamTable, true, agentColors);
		JScrollPane agentScroll = new JScrollPane(agentParamTable);
		// Add the scroll pane to this panel.
		agentPanel.add(agentScroll);

		this.add(agentPanel, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new GridLayout(1, params.agentParams.length));

		for (int i = 0; i < params.agentParams.length; i++) {
			JButton randomizeSeed = new JButton(new NewSeedAction(i, agentParamTable));
			buttons.add(randomizeSeed);
		}

		this.add(buttons, BorderLayout.SOUTH);
	}

	private final class NewSeedAction extends AbstractAction {

		private final int type;
		private final MixedValueJTable agentParamTable;
		private static final long serialVersionUID = 1L;

		private NewSeedAction(int type, MixedValueJTable agentParamTable) {
			super("New Seed");
			this.type = type;
			this.agentParamTable = agentParamTable;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Random r = new Random();
			params.agentParams[type].randomSeed = Math.abs(r.nextLong() % 100000l);
			agentParamTable.repaint();
		}
	}

	}
}
