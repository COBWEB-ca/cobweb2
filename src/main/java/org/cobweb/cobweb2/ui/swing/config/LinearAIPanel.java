/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.impl.ai.LinearWeightsController;
import org.cobweb.cobweb2.impl.ai.LinearWeightsControllerParams;
import org.cobweb.swingutil.ColorLookup;

/**
 * @author Igor
 *
 */
public class LinearAIPanel extends SettingsPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5135067595194478959L;

	private ColorLookup agentColors;

	public LinearAIPanel(ColorLookup agentColors) {
		super(true);
		this.agentColors = agentColors;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	private LinearWeightsControllerParams params;

	private JTabbedPane matrixTabPane;

	private List<DoubleMatrixModel> weightTables = new ArrayList<>();

	private List<String> pluginNames;

	private final class RandomizeAction extends AbstractAction {
		private DoubleMatrixModel model;
		public RandomizeAction(DoubleMatrixModel model) {
			super("Randomize");
			this.model = model;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Random r = new Random();
			for (int i = 0; i < model.data.length; i++) {
				for (int j = 0; j < model.data[i].length; j++) {
					model.data[i][j] = (double)Math.round(r.nextGaussian() * 1000) / 1000;
				}
			}
			model.reloadData();
		}
		private static final long serialVersionUID = 1L;
	}


	private final class ClearAction extends AbstractAction {
		private DoubleMatrixModel model;
		public ClearAction(DoubleMatrixModel model) {
			super("Clear");
			this.model = model;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Random r = new Random();
			for (int i = 0; i < model.data.length; i++) {
				for (int j = 0; j < model.data[i].length; j++) {
					model.data[i][j] = 0;
				}
			}
			model.reloadData();
		}
		private static final long serialVersionUID = 1L;
	}

	private final class CopyAction extends AbstractAction {
		private DoubleMatrixModel model;
		public CopyAction(DoubleMatrixModel model) {
			super("Copy to other types");
			this.model = model;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			for (DoubleMatrixModel target : weightTables) {
				if (target.equals(model))
					continue;
				for (int i = 0; i < model.data.length; i++) {
					for (int j = 0; j < model.data[i].length; j++) {
						target.data[i][j] = model.data[i][j];
					}
				}
				target.reloadData();
			}
		}
		private static final long serialVersionUID = 1L;
	}




	private class DoubleMatrixModel extends DefaultTableModel {

		/**
		 *
		 */
		private static final long serialVersionUID = 3074854765451031584L;

		private double[][] data;

		public DoubleMatrixModel(String[] inputNames, String[] outputNames,
				double[][] data) {
			super();
			this.data = data;
			setColumnCount(outputNames.length);
			setColumnIdentifiers(outputNames);
			setRowCount(inputNames.length );
			reloadData();
		}

		protected void reloadData() {
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					this.setValueAt(new Double(data[i][j]), i, j);
				}
			}
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			try {
				Double v = new Double(0);
				if (value instanceof String) {
					v = new Double(Double.parseDouble((String)value));
					data[row][column] = v.doubleValue();
				} else if (value instanceof Double) {
					v = ((Double)value);
				}
				super.setValueAt(v, row, column);
			} catch (NumberFormatException ex) {
				// Ignore bad value
			}
		}
	}

	@Override
	public void bindToParser(SimulationConfig p) {
		if (!(p.controllerParams instanceof LinearWeightsControllerParams)) {
			p.setControllerName(LinearWeightsController.class.getName());
			// SimulationConfig sets a blank default, but if the page already has settings, use them
			if (params != null)
				p.controllerParams = params;
		}
		params = (LinearWeightsControllerParams) p.controllerParams;

		removeAll();

		pluginNames = p.getPluginParameters();
		String[] fullInputNames = new String[LinearWeightsControllerParams.inputNames.length + pluginNames.size()];
		System.arraycopy(LinearWeightsControllerParams.inputNames, 0, fullInputNames, 0, LinearWeightsControllerParams.inputNames.length);
		for (int i = 0; i < pluginNames.size(); i++) {
			fullInputNames[LinearWeightsControllerParams.inputNames.length + i] = pluginNames.get(i);
		}

		weightTables.clear();
		matrixTabPane = new JTabbedPane();

		for (int i = 0; i < p.getAgentTypes(); i++) {
			DoubleMatrixModel matrixModel = new DoubleMatrixModel(fullInputNames, LinearWeightsControllerParams.outputNames, params.agentParams[i].dataInitial);
			weightTables.add(matrixModel);
			JTable matrix = new JTable(matrixModel);
			JScrollPane scrollpane = new JScrollPane(matrix);

			JPanel weightsPanel = new JPanel(new BorderLayout());
			Util.makeGroupPanel(weightsPanel, "Weights");

			JButton randomButton = new JButton(new RandomizeAction(matrixModel));
			JButton clearButton = new JButton(new ClearAction(matrixModel));
			JButton copyButton = new JButton(new CopyAction(matrixModel));
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(randomButton);
			buttonPanel.add(clearButton);
			buttonPanel.add(copyButton);

			weightsPanel.add(scrollpane, BorderLayout.CENTER);
			weightsPanel.add(buttonPanel, BorderLayout.SOUTH);

			prettyTable(matrix, scrollpane);

			matrixTabPane.addTab("Agent " + (i + 1), weightsPanel);
		}

		JTable paramTable = new MixedValueJTable(new ConfigTableModel(params.agentParams, "Agent "));
		JScrollPane paramScroll = new JScrollPane(paramTable);
		paramScroll.setPreferredSize(new Dimension(0, 80));
		Util.colorHeaders(paramTable, true, agentColors);
		Util.makeGroupPanel(paramScroll, "Controller Parameters");

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(paramScroll, BorderLayout.NORTH);
		panel.add(matrixTabPane, BorderLayout.CENTER);

		this.add(panel);
	}




	private void prettyTable(JTable matrix, JScrollPane scrollpane) {
		JTable rowHead = new JTable(LinearWeightsControllerParams.INPUT_COUNT + pluginNames.size(), 1);
		for (int i = 0; i < LinearWeightsControllerParams.INPUT_COUNT; i++) {
			rowHead.setValueAt(LinearWeightsControllerParams.inputNames[i], i, 0);
		}
		for (int i = 0; i < pluginNames.size(); i++) {
			rowHead.setValueAt(pluginNames.get(i), i + LinearWeightsControllerParams.INPUT_COUNT, 0);
		}
		scrollpane.setRowHeaderView(rowHead);
		LookAndFeel.installColorsAndFont(rowHead, "TableHeader.background","TableHeader.foreground", "TableHeader.font");
		LookAndFeel.installBorder(rowHead, "TableHeader.cellBorder");
		scrollpane.getRowHeader().setPreferredSize(new Dimension(130, 0));
		rowHead.setEnabled(false);

		JLabel corner = new JLabel("Inputs \\ Outputs", SwingConstants.CENTER);
		scrollpane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, corner);
		matrix.getTableHeader().setPreferredSize(new Dimension(0, 20));

		LookAndFeel.installColorsAndFont(corner, "TableHeader.background","TableHeader.foreground", "TableHeader.font");
		LookAndFeel.installBorder(corner, "TableHeader.cellBorder");

		matrix.setColumnSelectionAllowed(false);
		matrix.setRowSelectionAllowed(false);
		matrix.setCellSelectionEnabled(true);
		matrix.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

}
