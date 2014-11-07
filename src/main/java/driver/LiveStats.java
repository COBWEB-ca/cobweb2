package driver;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import cobweb.Environment.EnvironmentStats;
import cobweb.LocalUIInterface.TickEventListener;
import cobweb.UIInterface;


public class LiveStats implements TickEventListener {

	JFrame graph = new JFrame("Statistics");

	int frame = 0;


	static final int frameskip = 50;


	XYSeries agentData = new XYSeries("Agents");
	XYSeries foodData = new XYSeries("Food");

	XYSeriesCollection data = new XYSeriesCollection();
	JFreeChart plot = ChartFactory.createXYLineChart(
			"Agent and Food count"
			, "Time"
			, "Count"
			, data
			, PlotOrientation.VERTICAL
			, true
			, false
			, false);

	UIInterface ui;

	public LiveStats(UIInterface ui) {
		this.ui = ui;
		graph.setSize(500, 500);
		ChartPanel cp = new ChartPanel(plot, true);
		graph.add(cp);
		plot.setAntiAlias(true);
		plot.setNotify(false);

		data.addSeries(agentData);
		data.addSeries(foodData);
	}

	public void TickPerformed(long currentTick) {


		EnvironmentStats stats = ui.getStatistics();
		long agentCount = 0;
		for (long count : stats.agentCounts) {
			agentCount += count;
		}
		long foodCount = 0;
		for (long count : stats.foodCounts) {
			foodCount += count;
		}

		agentData.add(currentTick, agentCount);
		foodData.add(currentTick, foodCount);

		if (frame++ == frameskip) {
			frame = 0;
			plot.setNotify(true);
			plot.setNotify(false);
		}
	}

	public void toggleGraphVisible() {
		graph.setVisible(!graph.isVisible());
	}

}
