/**
 *
 */
package driver.config;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import cwcore.LinearWeightsController;
import cwcore.LinearWeightsControllerParams;
import cwcore.complexParams.ComplexEnvironmentParams;
import driver.Parser;
import driver.SettingsPanel;

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
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					this.setValueAt(new Double(data[i][j]), i, j);
				}
			}
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			Double v = new Double(0);
			try {
				if (value instanceof String) {
					v = Double.parseDouble((String)value);
					data[row][column] = v;
				} else if (value instanceof Double) {
					v = ((Double)value).doubleValue();
				}
			} catch (NumberFormatException ex) {
				// Ignore bad value
			}
			super.setValueAt(v, row, column);
		}
	}

	@Override
	public void bindToParser(Parser p) {
		ComplexEnvironmentParams ep = p.getEnvParams();
		if (!(ep.controllerParams instanceof LinearWeightsControllerParams)) {
			p.getEnvParams().controllerName = LinearWeightsController.class.getName();
			if (params == null)
				params = new LinearWeightsControllerParams();
			p.getEnvParams().controllerParams = params;
		} else {
			params = (LinearWeightsControllerParams) p.getEnvParams().controllerParams;
		}

		removeAll();

		matrix = new JTable(new DoubleMatrixModel(LinearWeightsController.inputNames, LinearWeightsController.outputNames, params.data));

		scrollpane = new JScrollPane(matrix);

		prettyTable();

		this.add(scrollpane);
	}




	private void prettyTable() {
		JTable rowHead = new JTable(LinearWeightsController.INPUT_COUNT, 1);
		for (int i = 0; i < LinearWeightsController.INPUT_COUNT; i++) {
			rowHead.setValueAt(LinearWeightsController.inputNames[i], i, 0);
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
