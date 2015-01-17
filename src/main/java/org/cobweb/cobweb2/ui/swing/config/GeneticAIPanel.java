/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.text.NumberFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.ai.GeneticController;
import org.cobweb.cobweb2.ai.GeneticControllerParams;
import org.cobweb.swingutil.ColorLookup;
import org.cobweb.swingutil.binding.BoundJFormattedTextField;

final class GeneticAIPanel extends SettingsPanel {
	private static final long serialVersionUID = 1139521733160862828L;
	private GeneticControllerParams params;
	private BoundJFormattedTextField seed;
	private ColorLookup agentColors;

	public GeneticAIPanel(ColorLookup agentColors) {
		this.agentColors = agentColors;
	}

	@Override
	public void bindToParser(SimulationConfig p) {
		if (!(p.getControllerParams() instanceof GeneticControllerParams)) {
			p.getEnvParams().controllerName = GeneticController.class.getName();
			if (params == null)
				params = new GeneticControllerParams(p);

			p.setControllerParams(params);

		} else {
			params = (GeneticControllerParams) p.getControllerParams();
		}
		updateBoxes();
	}

	private void updateBoxes() {
		setLayout(new BorderLayout());
		seed = new BoundJFormattedTextField(params, "randomSeed", NumberFormat
				.getIntegerInstance());
		this.removeAll();
		seed.setColumns(5);
		JPanel random = new JPanel();
		random.add(new JLabel(seed.getLabelText()));
		random.add(seed);
		JButton makeRandom = new JButton("Generate");
		makeRandom.addActionListener(new SeedRandomListener(seed));
		random.add(makeRandom);

		this.add(random, BorderLayout.NORTH);

		JPanel agentPanel = new JPanel();
		agentPanel.setLayout(new BoxLayout(agentPanel, BoxLayout.X_AXIS));
		Util.makeGroupPanel(agentPanel, "Agent Parameters");

		MixedValueJTable agentParamTable = new MixedValueJTable(
				new ConfigTableModel(params.agentParams.agentParams, "Agent "));

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