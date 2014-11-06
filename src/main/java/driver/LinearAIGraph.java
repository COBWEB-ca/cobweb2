package driver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import cwcore.LinearWeightsController;

public class LinearAIGraph extends JFrame implements WindowListener, ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 674068154319803208L;

	ChartPanel chartPan;

	DefaultCategoryDataset catd = new DefaultCategoryDataset();
	JFreeChart chart;

	private Timer refreshTimer;

	public LinearAIGraph() {
		super("AI output distribution");

		for (String x : LinearWeightsController.outputNames) {
			catd.addValue(0, x, "");
		}
		chart = ChartFactory.createBarChart("AI output distribution", "Output", "Value", catd, PlotOrientation.VERTICAL, true, false, false);
		chartPan = new ChartPanel(chart, true);


		this.add(chartPan);
		this.addWindowListener(this);
		refreshTimer = new Timer(100, this);
		this.pack();
	}

	public void windowActivated(WindowEvent e) {
		// Nothing
	}

	public void windowClosed(WindowEvent e) {
		// Nothing
	}

	public void windowClosing(WindowEvent e) {
		refreshTimer.stop();
	}

	public void windowDeactivated(WindowEvent e) {
		// Nothing
	}

	public void windowDeiconified(WindowEvent e) {
		// Nothing
	}

	public void windowIconified(WindowEvent e) {
		// Nothing
	}

	public void windowOpened(WindowEvent e) {
		refreshTimer.start();
	}

	/**
	 * Refresh timer function
	 */
	public void actionPerformed(ActionEvent arg0) {
		double data[] = LinearWeightsController.getRunningOutputMean();
		for (int i = 0; i < LinearWeightsController.OUTPUT_COUNT; i++) {
			catd.setValue(data[i], LinearWeightsController.outputNames[i], "");
		}
	}

}
