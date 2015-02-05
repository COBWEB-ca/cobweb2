/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumnModel;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.impl.ai.BehaviorArray;
import org.cobweb.cobweb2.impl.ai.GeneticController;
import org.cobweb.cobweb2.impl.ai.GeneticControllerParams;
import org.cobweb.cobweb2.impl.ai.GeneticStateAgentParams;
import org.cobweb.swingutil.ColorLookup;

final class GeneticAIPanel extends SettingsPanel {

	private static final long serialVersionUID = 1139521733160862828L;
	private GeneticControllerParams params;
	private ColorLookup agentColors;
	private Dialog parentWindow;

	public GeneticAIPanel(ColorLookup agentColors, Dialog parentWindow) {
		this.agentColors = agentColors;
		this.parentWindow = parentWindow;
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
			JButton saveMatrix = new JButton(new SaveMatrixAction(i));

			JPanel typePanel = new JPanel(new GridLayout(2, 1));
			typePanel.add(randomizeSeed);
			typePanel.add(saveMatrix);
			buttons.add(typePanel);
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

	private final class SaveMatrixAction extends AbstractAction {

		private final int type;
		private static final long serialVersionUID = 1L;

		private SaveMatrixAction(int type) {
			super("Save Matrix");
			this.type = type;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			FileDialog theDialog = new FileDialog(parentWindow,
					"Save AI matrix", FileDialog.SAVE);
			theDialog.setFile("*.tsv");
			theDialog.setVisible(true);
			String directory = theDialog.getDirectory();
			String file = theDialog.getFile();

			if (file != null && directory != null) {
				try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
						new File(directory + file))))) {

					GeneticStateAgentParams agentParams = params.agentParams[type];

					BehaviorArray array = new GeneticController(null, agentParams).ga;

					// Header
					// Input bits
					out.print("Energy 1"); out.print('\t');
					out.print("Energy 2"); out.print('\t');
					out.print("Direction 1"); out.print('\t');
					out.print("Direction 2"); out.print('\t');
					out.print("See Type 1"); out.print('\t');
					out.print("See Type 2"); out.print('\t');
					out.print("See Distance 1"); out.print('\t');
					out.print("See Distance 2"); out.print('\t');
					for (int i = 0; i < agentParams.memoryBits; i++) {
						out.print("Memory " + (i + 1)); out.print('\t');
					}
					for (int i = 0; i < agentParams.communicationBits; i++) {
						out.print("Communication " + (i + 1)); out.print('\t');
					}
					for (Entry<String, Integer> state : agentParams.stateSizes.entrySet()) {
						String name = state.getKey();
						for (int i = 0; i < state.getValue(); i++) {
							out.print(name + " " + (i + 1));  out.print('\t');
						}
					}
					out.print('\t');

					// Output bits
					out.print("Move 1"); out.print('\t');
					out.print("Move 2"); out.print('\t');
					for (int i = 0; i < agentParams.memoryBits; i++) {
						out.print("Memory " + (i + 1)); out.print('\t');
					}
					for (int i = 0; i < agentParams.communicationBits; i++) {
						out.print("Communication " + (i + 1)); out.print('\t');
					}
					out.print("Reproduce Asex"); out.print('\t');

					out.println();


					// Data
					for (int i = 0; i < array.getSize(); i++) {
						int[] outp = array.getOutput(i);

						// input bits
						for (int b = 0; b < array.inputSize; b++) {
							out.print((i & (1 << b)) != 0 ? '1' : '0'); out.print('\t');
						}
						out.print('\t');

						for (int j = 0; j < outp.length; j++) {
							int outPart = outp[j];
							for (int b = 0; b < array.outputSize[j]; b++)  {
								out.print((outPart & (1 << b)) != 0 ? '1' : '0'); out.print('\t');
							}
						}
						out.println();
					}

				} catch (IOException ex) {
					throw new RuntimeException("Unable to save matrix", ex);
				}

			}



		}
	}
}
