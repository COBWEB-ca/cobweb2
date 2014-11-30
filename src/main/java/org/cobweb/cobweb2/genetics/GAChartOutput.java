package org.cobweb.cobweb2.genetics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cobweb.cobweb2.ui.swing.ColorLookup;
import org.cobweb.swingutil.JComponentWaiter;
import org.cobweb.swingutil.TypeColorEnumeration;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

// TODO: turn into ViewerPlugin
public class GAChartOutput implements ActionListener {

	/** Charts that represent GA outputs */
	private JFreeChart[] gene_status_distribution_chart;
	private JFreeChart[] gene_value_distribution_chart;

	/** x-vectors of the GA outputs */
	private double[] gene_status_distribution_range = new double[GATracker.GENE_STATUS_DISTRIBUTION_SIZE];
	private double[] gene_value_distribution_range = new double[GATracker.GENE_VALUE_DISTRIBUTION_SIZE];

	/** Data of GA output charts .*/
	private DefaultXYDataset[] gene_status_distribution_data;
	private DefaultXYDataset[] gene_value_distribution_data;

	/** Chart panels of GA output that hold the charts. */
	private JPanel gene_status_distribution_panel;
	private JPanel gene_value_distribution_panel;

	/** Chart frame of GA that holds the charts (and their ChartPanels). */
	private JFrame chart_display_frame;

	/** Buttons that display GA outputs on the chart frame. */
	private JButton gene_status_distribution_button =
		new JButton("Genotype-Phenotype Correlation Value");
	private JButton gene_value_distribution_button =
		new JButton("Genotype Value");

	/** Current component on display. */
	private JPanel current_display;

	private ColorLookup colorMap = TypeColorEnumeration.getInstance();

	public void actionPerformed(java.awt.event.ActionEvent e) {

		/* Give focus to the gene_status_distribution plot and remove
		 * chart currently displayed from the display frame.*/
		if (e.getSource().equals(gene_status_distribution_button)) {
			chart_display_frame.remove(current_display);
			chart_display_frame.getContentPane()
			.add(gene_status_distribution_panel, BorderLayout.CENTER);
			chart_display_frame.setName("Genotype-Phenotype Correlation Value Distribution");
			chart_display_frame.setVisible(true);
			current_display = gene_status_distribution_panel;

			/* Give focus to the gene_value_distribution plot and remove
			 * chart currently displayed from the display frame.*/
		} else if (e.getSource().equals(gene_value_distribution_button)) {
			chart_display_frame.remove(current_display);
			chart_display_frame.getContentPane()
			.add(gene_value_distribution_panel, BorderLayout.CENTER);
			chart_display_frame.setName("Genotype Value Distribution");
			chart_display_frame.setVisible(true);
			current_display = gene_value_distribution_panel;
		}
		chart_display_frame.repaint();
	}

	/** Initialize a chart representation of gene status distribution among agents. */
	public void initGeneStatusDistributionPlot(String[] names) {
		chart_display_frame.setName("Genotype-Phenotype Correlation Value Distribution");
		gene_status_distribution_panel = new JPanel(new GridLayout(1,geneCount));
		for (int i = 0; i < geneCount; i++) {
			gene_status_distribution_data[i] = new DefaultXYDataset();
			gene_status_distribution_chart[i] = ChartFactory.createXYAreaChart(
					"Gene " + (i+1) + " : " + names[i],
					"Phenotype Multiplier", "Number of Agents",
					gene_status_distribution_data[i], PlotOrientation.VERTICAL,
					true,    // legend?
					true,    // tooltips?
					false    // URLs?
			);

			XYItemRenderer renderer = gene_status_distribution_chart[i].getXYPlot().getRenderer();

			for (int agent = 0; agent < numAgentTypes; agent++) {
				renderer.setSeriesPaint(agent, colorMap.getColor(agent, 0));
			}

			ChartPanel cp = new ChartPanel(gene_status_distribution_chart[i]);
			gene_status_distribution_panel.add(cp);
			gene_status_distribution_chart[i].addChangeListener(cp);
		}
		chart_display_frame.getContentPane().add(gene_status_distribution_panel, BorderLayout.CENTER);
		current_display = gene_status_distribution_panel;
		initGeneStatusDistributionRangeVector(); // Initialize x vector
	}

	private int numAgentTypes;

	private int geneCount;


	public GAChartOutput(int agents, int genes, String[] names) {
		numAgentTypes = agents;
		geneCount = genes;

		gene_value_distribution_chart = new JFreeChart[geneCount];
		gene_status_distribution_chart = new JFreeChart[geneCount];

		gene_status_distribution_data  = new DefaultXYDataset[geneCount];
		gene_value_distribution_data = new DefaultXYDataset[geneCount];

		gene_value_distribution_panel = new JPanel(new GridLayout(1,geneCount));
		for (int i = 0; i < geneCount; i++) {
			gene_value_distribution_data[i] = new DefaultXYDataset();
			gene_value_distribution_chart[i] = ChartFactory.createXYAreaChart(
					"Gene " + (i+1) + " : " + names[i],
					"Gene Value", "Number of Agents",
					gene_value_distribution_data[i], PlotOrientation.VERTICAL,
					true,    // legend?
					true,    // tooltips?
					false    // URLs?
			);

			XYItemRenderer renderer = gene_value_distribution_chart[i].getXYPlot().getRenderer();
			for (int agent = 0; agent < numAgentTypes; agent++) {
				renderer.setSeriesPaint(agent, colorMap.getColor(agent, 0));
			}


			ChartPanel cp = new ChartPanel(gene_value_distribution_chart[i]);
			gene_value_distribution_panel.add(cp);
			gene_value_distribution_chart[i].addChangeListener(cp);
		}
		current_display = gene_value_distribution_panel;
		initGeneValueDistributionRangeVector(); // Initialize x vector

		if (chart_display_frame != null) {
			chart_display_frame.dispose();
			chart_display_frame = null;
		}
		chart_display_frame = new JFrame("Gene Statistics");
		refreshWaiter = new JComponentWaiter(chart_display_frame);
		JPanel button_panel = new JPanel();

		// If we are tracking the gene status distribution
		initGeneStatusDistributionPlot(names);

		// Initialize button
		gene_status_distribution_button.addActionListener(this);
		button_panel.add(gene_status_distribution_button);
		gene_value_distribution_button.addActionListener(this);
		button_panel.add(gene_value_distribution_button);

		chart_display_frame.getContentPane().add(button_panel, BorderLayout.SOUTH);
		chart_display_frame.setSize(new Dimension(900, 460));
		chart_display_frame.setVisible(true);
	}


	/** Initialize the gene_status_distribution_range x-vector. */
	public void initGeneStatusDistributionRangeVector() {
		for (int i = 0; i < GATracker.GENE_STATUS_DISTRIBUTION_SIZE; i++) {
			// Rounding the number off to the 4 significand digits
			gene_status_distribution_range[i] = Math.round(2*Math.abs(Math.sin(i*Math.PI/180)*10000))/10000.0;
		}
	}

	/** Initialize the gene_value_distribution_range x-vector. */
	public void initGeneValueDistributionRangeVector() {
		for (int i = 0; i < GATracker.GENE_VALUE_DISTRIBUTION_SIZE; i++) {
			// Rounding the number off to the 4 significand digits
			gene_value_distribution_range[i] = i;

		}
	}

	JComponentWaiter refreshWaiter;

	/** Update the gene_status_distribution data */
	public void updateGeneStatusDistributionData(double[][][] value, double[][][] status) {
		/* 
		 * Collecting all data into one array, then replacing everything at once
		 * to hopefully fix race condition in JFreeChart
		 */
		double[][][][][] new_datasets = new double[geneCount][][][][];

		if (refreshWaiter.isReadyToRefresh()) {
			for (int j = 0; j < geneCount; j++) {
				new_datasets[j] = new double[numAgentTypes][2][][];

				for (int i = 0; i < numAgentTypes; i++) {
					double[][] newx = {gene_status_distribution_range, status[i][j]};
					new_datasets[j][i][0] = newx;
					double[][] newy = {gene_value_distribution_range, value[i][j]};
					new_datasets[j][i][1] = newy;
				}
			}

			for (int j = 0; j < geneCount; j++) {
				for (int i = 0; i < numAgentTypes; i++) {
					String key = "Agent " + (i+1);
					gene_status_distribution_data[j].addSeries(key, new_datasets[j][i][0]);
					gene_value_distribution_data[j].addSeries(key, new_datasets[j][i][1]);
				}
			}

			// If wait is true, there might be a deadlock pressing the stop button
			refreshWaiter.refresh(false);
		}

	}
}
