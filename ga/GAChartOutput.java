package ga;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import cobweb.ColorLookup;
import cobweb.TypeColorEnumeration;
import cwcore.ComplexEnvironment;
import driver.GUI;


public class GAChartOutput implements ActionListener {

	/** Grid dimensions of Cobweb */
	public static int grid_height;
	public static int grid_width;

	/** Charts that represent GA outputs */
	private static JFreeChart[] gene_status_distribution_chart = new JFreeChart[GeneticCode.NUM_GENES];
	private static JFreeChart[] gene_value_distribution_chart = new JFreeChart[GeneticCode.NUM_GENES];

	/** x-vectors of the GA outputs */
	private static double[] gene_status_distribution_range = new double[GATracker.GENE_STATUS_DISTRIBUTION_SIZE];
	private static double[] gene_value_distribution_range = new double[GATracker.GENE_VALUE_DISTRIBUTION_SIZE];

	/** Data of GA output charts .*/
	private static DefaultXYDataset[] gene_status_distribution_data = new DefaultXYDataset[GeneticCode.NUM_GENES];
	private static DefaultXYDataset[] gene_value_distribution_data = new DefaultXYDataset[GeneticCode.NUM_GENES];

	/** Chart panels of GA output that hold the charts. */
	private static JPanel gene_status_distribution_panel;
	private static JPanel gene_value_distribution_panel;

	/** Chart frame of GA that holds the charts (and their ChartPanels). */
	private static JFrame chart_display_frame;

	/** Buttons that display GA outputs on the chart frame. */
	private static JButton gene_status_distribution_button
		= new JButton("Genotype-Phenotype Correlation Value");
	private static JButton gene_value_distribution_button
		= new JButton("Genotype Value");

	/** Current component on display. */
	private static Component current_display;

	/** Update frequency of chart (time step per update) */
	public static int update_frequency = 1;

	private static ColorLookup colorMap = TypeColorEnumeration.getInstance();

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
	}

	/** Initialize the plots that display GA outputs*/
	public void initPlots() {
		if (chart_display_frame != null) {
			chart_display_frame.dispose();
			chart_display_frame = null;
		}
		chart_display_frame = new JFrame("Gene Statistics");
		JPanel button_panel = new JPanel();

		// If we are tracking the gene value distribution
		if (GATracker.getTrackGeneValueDistribution()) {
			initGeneValueDistributionPlot();

			// Initialize button
			gene_value_distribution_button.addActionListener(this);
			button_panel.add(gene_value_distribution_button);

		}

		// If we are tracking the gene status distribution
		if (GATracker.getTrackGeneStatusDistribution()) {
			initGeneStatusDistributionPlot();

			// Initialize button
			gene_status_distribution_button.addActionListener(this);
			button_panel.add(gene_status_distribution_button);

		}

		chart_display_frame.getContentPane().add(button_panel, BorderLayout.SOUTH);
		chart_display_frame.setSize(new Dimension(900, 460));
		chart_display_frame.setVisible(true);
	}

	/** Initialize a chart representation of gene status distribution among agents. */
	public static void initGeneStatusDistributionPlot() {
		chart_display_frame.setName("Genotype-Phenotype Correlation Value Distribution");
		gene_status_distribution_panel = new JPanel(new GridLayout(1,GeneticCode.NUM_GENES));
		for (int i = 0; i < GeneticCode.NUM_GENES; i++) {
			gene_status_distribution_data[i] = new DefaultXYDataset();
			gene_status_distribution_chart[i] = ChartFactory.createXYAreaChart(
		             "Distribution of Genotype-Phenotype Correlation of Gene #" + (i+1),
		             "Geneotype-Phenotype Correlation Value", "Number of Agents",
		             gene_status_distribution_data[i], PlotOrientation.VERTICAL,
		             true,    // legend?
		             true,    // tooltips?
		             false    // URLs?
		         );

			XYItemRenderer renderer = gene_status_distribution_chart[i].getXYPlot().getRenderer();
			for (int agent = 0; agent < GUI.numAgentTypes; agent++) {
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

	/** Initialize a chart representation of gene values distribution among agents. */
	public static void initGeneValueDistributionPlot() {
		chart_display_frame.setName("Genotype Value Distribution");
		gene_value_distribution_panel = new JPanel(new GridLayout(1,GeneticCode.NUM_GENES));
		for (int i = 0; i < GeneticCode.NUM_GENES; i++) {
			gene_value_distribution_data[i] = new DefaultXYDataset();
			gene_value_distribution_chart[i] = ChartFactory.createXYAreaChart(
					"Distribution of Gene #" + (i+1) + " Value",
		             "Genotype Value", "Number of Agents",
		             gene_value_distribution_data[i], PlotOrientation.VERTICAL,
		             true,    // legend?
		             true,    // tooltips?
		             false    // URLs?
		         );

			XYItemRenderer renderer = gene_value_distribution_chart[i].getXYPlot().getRenderer();
			for (int agent = 0; agent < GUI.numAgentTypes; agent++) {
				renderer.setSeriesPaint(agent, colorMap.getColor(agent, 0));
			}


	        ChartPanel cp = new ChartPanel(gene_value_distribution_chart[i]);
	        gene_value_distribution_panel.add(cp);
	        gene_value_distribution_chart[i].addChangeListener(cp);
		}
		chart_display_frame.getContentPane().add(gene_value_distribution_panel, BorderLayout.CENTER);
		current_display = gene_value_distribution_panel;
		initGeneValueDistributionRangeVector(); // Initialize x vector
	}

	/** Initialize the gene_status_distribution_range x-vector. */
	public static void initGeneStatusDistributionRangeVector() {
		for (int i = 0; i < GATracker.GENE_STATUS_DISTRIBUTION_SIZE; i++) {
			// Rounding the number off to the 4 significand digits
			gene_status_distribution_range[i] = new Double(Math.round(2*Math.abs(Math.sin(i*Math.PI/180)*10000)))/10000;
		}
	}

	/** Initialize the gene_value_distribution_range x-vector. */
	public static void initGeneValueDistributionRangeVector() {
		for (int i = 0; i < GATracker.GENE_VALUE_DISTRIBUTION_SIZE; i++) {
			// Rounding the number off to the 4 significand digits
			gene_value_distribution_range[i] = i;

		}
	}

	/** Update the gene_status_distribution data */
	public static void updateGeneStatusDistributionData(double[][][] y_vector) {
		for (int j = 0; j < GeneticCode.NUM_GENES; j++) {
			for (int i = 0; i < ComplexEnvironment.AGENT_TYPES; i++) {
				double[][] new_data = {gene_status_distribution_range, y_vector[i][j]};
				String key = "Agent " + (i+1);
				gene_status_distribution_data[j].addSeries(key, new_data);
			}

			// Set an upper bound for the y-axis
//			gene_status_distribution_chart[j].getXYPlot().getRangeAxis().setUpperBound(
//					(double)grid_width*grid_height/4);
		}
	}

	/** Update the gene_value_distribution data */
	public static void updateGeneValueDistributionData(double[][][] y_vector) {
		for (int j = 0; j < GeneticCode.NUM_GENES; j++) {
			for (int i = 0; i < ComplexEnvironment.AGENT_TYPES; i++) {
				double[][] new_data = {gene_value_distribution_range, y_vector[i][j]};
				String key = "Agent " + (i+1);
				gene_value_distribution_data[j].addSeries(key, new_data);

			}
			// Set an upper bound for the y-axis
//			gene_value_distribution_chart[j].getXYPlot().getRangeAxis().setUpperBound(
//					(double)grid_width*grid_height/4);

		}
	}
}
