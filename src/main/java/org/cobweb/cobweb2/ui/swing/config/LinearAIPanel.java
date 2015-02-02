/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

	private JTable matrix;

	private LinearWeightsControllerParams params;

	private JScrollPane scrollpane;

	private DoubleMatrixModel matrixModel;

	private List<String> pluginNames;

	private final class RandomButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Random r = new Random();
			for (int i = 0; i < matrixModel.data.length; i++) {
				for (int j = 0; j < matrixModel.data[i].length; j++) {
					matrixModel.data[i][j] = (double)Math.round(r.nextGaussian() * 1000) / 1000;
				}
			}
			matrixModel.reloadData();
		}
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
		String[] fullInputNames = new String[params.inputNames.length + pluginNames.size()];
		System.arraycopy(params.inputNames, 0, fullInputNames, 0, params.inputNames.length);
		for (int i = 0; i < pluginNames.size(); i++) {
			fullInputNames[params.inputNames.length + i] = pluginNames.get(i);
		}

		matrixModel = new DoubleMatrixModel(fullInputNames, params.outputNames, params.data);
		matrix = new JTable(matrixModel);
		scrollpane = new JScrollPane(matrix);

		JPanel weightsPanel = new JPanel(new BorderLayout());
		{
			Util.makeGroupPanel(weightsPanel, "Weights");
			JButton randomButton = new JButton("Randomize");
			randomButton.addActionListener(new RandomButtonListener());
			JPanel randomPanel = new JPanel();
			randomPanel.add(randomButton);

			weightsPanel.add(scrollpane, BorderLayout.CENTER);
			weightsPanel.add(randomPanel, BorderLayout.SOUTH);
		}

		JTable paramTable = new MixedValueJTable(new ConfigTableModel(params.agentParams, "Agent "));
		JScrollPane paramScroll = new JScrollPane(paramTable);
		paramScroll.setPreferredSize(new Dimension(0, 80));
		Util.colorHeaders(paramTable, true, agentColors);
		Util.makeGroupPanel(paramScroll, "Controller Parameters");

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(paramScroll, BorderLayout.NORTH);
		panel.add(weightsPanel, BorderLayout.CENTER);

		prettyTable();

		this.add(panel);
	}




	private void prettyTable() {
		JTable rowHead = new JTable(params.INPUT_COUNT + pluginNames.size(), 1);
		for (int i = 0; i < params.INPUT_COUNT; i++) {
			rowHead.setValueAt(params.inputNames[i], i, 0);
		}
		for (int i = 0; i < pluginNames.size(); i++) {
			rowHead.setValueAt(pluginNames.get(i), i + params.INPUT_COUNT, 0);
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
