package org.cobweb.cobweb2.ui.swing.ai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.cobweb.cobweb2.ai.LinearWeightsController;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class LinearAIGraph extends JFrame implements WindowListener, ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 674068154319803208L;

	private ChartPanel chartPan;

	private DefaultCategoryDataset catd = new DefaultCategoryDataset();
	private JFreeChart chart;

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

	@Override
	public void windowActivated(WindowEvent e) {
		// Nothing
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// Nothing
	}

	@Override
	public void windowClosing(WindowEvent e) {
		refreshTimer.stop();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// Nothing
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// Nothing
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// Nothing
	}

	@Override
	public void windowOpened(WindowEvent e) {
		refreshTimer.start();
	}

	/**
	 * Refresh timer function
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		double data[] = LinearWeightsController.getRunningOutputMean();
		for (int i = 0; i < LinearWeightsController.OUTPUT_COUNT; i++) {
			catd.setValue(data[i], LinearWeightsController.outputNames[i], "");
		}
	}

}
