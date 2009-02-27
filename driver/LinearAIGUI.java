/**
 *
 */
package driver;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Writer;

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

/**
 * @author Igor
 *
 */
public class LinearAIGUI extends SettingsPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5135067595194478959L;

	public LinearAIGUI() {
		super(true);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		data = LinearWeightsController.getDefaultWeights();

		matrix = new JTable(new DoubleMatrixModel(LinearWeightsController.inputNames, LinearWeightsController.outputNames, data));


		JScrollPane scrollpane = new JScrollPane(matrix);

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

		this.add(scrollpane);
	}

	JTable matrix;

	double[][] data;


	private class DoubleMatrixModel extends DefaultTableModel {

		/**
		 *
		 */
		private static final long serialVersionUID = 3074854765451031584L;

		public DoubleMatrixModel(String[] inputNames, String[] outputNames,
				double[][] data) {
			super();
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
				} else {
					System.out.println(value.getClass());
				}
			} catch (NumberFormatException ex) {
				return;
			}
			super.setValueAt(v, row, column);
		}
	}




	@Override
	public void readFromParser(Parser p) {
		String conf = p.ControllerConfig;
		int i = 0;
		int j = 0;
		for (String e : conf.split(",")) {
			matrix.setValueAt(Double.parseDouble(e), i, j);
			data[i][j] = Double.parseDouble(e);
			if (++j >= data[i].length) {
				j = 0;
				if (++i >= data.length) return;
			}
		}
	}



	@Override
	public void writeXML(Writer out) throws IOException {
		for (double[] element : data) {
			for (double element2 : element) {
				out.write(String.format("%f", element2));
				out.write(",");
			}
			out.write("\n");
		}
	}
}
