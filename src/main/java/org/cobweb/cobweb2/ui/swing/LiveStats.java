package org.cobweb.cobweb2.ui.swing;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

import org.cobweb.cobweb2.core.EnvironmentStats;
import org.cobweb.cobweb2.core.UIInterface;
import org.cobweb.cobweb2.core.LocalUIInterface.TickEventListener;
import org.cobweb.cobweb2.ui.ViewerClosedCallback;
import org.cobweb.cobweb2.ui.ViewerPlugin;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class LiveStats implements TickEventListener, ViewerPlugin {

	JFrame graph = new JFrame("Population");

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
		graph.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				if (onClosed != null)
					onClosed.viewerClosed();
			}
		});

		graph.setSize(500, 500);
		ChartPanel cp = new ChartPanel(plot, true);
		graph.add(cp);
		plot.setAntiAlias(true);
		plot.setNotify(false);

		data.addSeries(agentData);
		data.addSeries(foodData);

		ui.AddTickEventListener(this);
	}

	@Override
	public void dispose() {
		ui.RemoveTickEventListener(this);
		graph.dispose();
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

	@Override
	public String getName() {
		return "Population Graph";
	}

	@Override
	public void on() {
		graph.setVisible(true);

	}

	@Override
	public void off() {
		graph.setVisible(false);
	}

	ViewerClosedCallback onClosed;
	@Override
	public void setClosedCallback(ViewerClosedCallback onClosed) {
		this.onClosed = onClosed;
	}

}
