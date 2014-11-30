/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import org.cobweb.cobweb2.ai.LinearWeightsController;
import org.cobweb.cobweb2.ai.LinearWeightsControllerParams;
import org.cobweb.cobweb2.ui.swing.SettingsPanel;
import org.cobweb.cobweb2.ui.swing.SimulationConfig;

/**
 * @author Igor
 *
 */
public class LinearAIPanel extends SettingsPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5135067595194478959L;

	public LinearAIPanel() {
		super(true);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	private JTable matrix;

	private LinearWeightsControllerParams params;

	private JScrollPane scrollpane;

	private DoubleMatrixModel matrixModel;

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
		if (!(p.getControllerParams() instanceof LinearWeightsControllerParams)) {
			p.getEnvParams().controllerName = LinearWeightsController.class.getName();
			if (params == null)
				params = new LinearWeightsControllerParams();
			p.setControllerParams(params);
		} else {
			params = (LinearWeightsControllerParams) p.getControllerParams();
		}

		removeAll();

		String[] fullInputNames = new String[LinearWeightsController.inputNames.length + LinearWeightsControllerParams.pluginNames.size()];
		System.arraycopy(LinearWeightsController.inputNames, 0, fullInputNames, 0, LinearWeightsController.inputNames.length);
		for (int i = 0; i < LinearWeightsControllerParams.pluginNames.size(); i++) {
			fullInputNames[LinearWeightsController.inputNames.length + i] = LinearWeightsControllerParams.pluginNames.get(i);
		}

		matrixModel = new DoubleMatrixModel(fullInputNames, LinearWeightsController.outputNames, params.data);
		matrix = new JTable(matrixModel);

		scrollpane = new JScrollPane(matrix);

		JPanel panel = new JPanel();
		BorderLayout bl = new BorderLayout();
		panel.setLayout(bl);
		panel.add(scrollpane, BorderLayout.CENTER);

		JPanel randomPanel = new JPanel();
		JButton randomButton = new JButton("Randomize");
		randomButton.addActionListener(new RandomButtonListener());
		randomPanel.add(randomButton);
		panel.add(randomPanel, BorderLayout.SOUTH);

		prettyTable();

		this.add(panel);
	}




	private void prettyTable() {
		JTable rowHead = new JTable(LinearWeightsController.INPUT_COUNT + LinearWeightsControllerParams.pluginNames.size(), 1);
		for (int i = 0; i < LinearWeightsController.INPUT_COUNT; i++) {
			rowHead.setValueAt(LinearWeightsController.inputNames[i], i, 0);
		}
		for (int i = 0; i < LinearWeightsControllerParams.pluginNames.size(); i++) {
			rowHead.setValueAt(LinearWeightsControllerParams.pluginNames.get(i), i + LinearWeightsController.INPUT_COUNT, 0);
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
