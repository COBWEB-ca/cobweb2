/**
 *
 */
package org.cobweb.cobweb2.ui.swing.applet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.ui.swing.LiveStats;
import org.cobweb.cobweb2.ui.swing.SimulatorUI;


/**
 * Applet version of COBWEB2
 */
public class Cobweb2Applet extends JApplet { // NO_UCD. Stop UCDetector from labeling as unused class

	private class ExpSelectorListener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox)e.getSource();
			String expname = (String)cb.getSelectedItem();
			loadSimulation(expname);
		}

	}

	/**
	 *
	 */
	private static final long serialVersionUID = 3127350835002502812L;
	Map<String, String> experements = new LinkedHashMap<String, String>();
	SimulatorUI ui;
	Simulation simulation;

	JPanel controls;
	SimulationConfig parser;

	String currentexp;

	ExperementSelector expselector;

	JLabel statsLabel;

	LiveStats statsUpdater;

	@Override
	public void init() {
		super.init();

		setSize(580,660);

		setLayout(new BorderLayout());

		experements.put("Baseline 2009", "baseline 2009.xml");
		experements.put("Boom and Bust", "boom and bust 2 applet.xml");
		experements.put("Exponential Growth", "Exponential Growth Experiment.xml");
		experements.put("Central Place", "central place applet.xml");
		experements.put("Cheaters vs Cooperators", "cheaters vs cooperators.xml");

		currentexp = "Baseline 2009";

		controls = new JPanel();

		JLabel selectorlabel = new JLabel("Experiment:");
		controls.add(selectorlabel);

		expselector = new ExperementSelector(experements);
		controls.add(expselector);

		JButton resetbutton = new JButton("Reset");
		controls.add(resetbutton);
		resetbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadSimulation(currentexp);
			}
		});

		statsLabel = new JLabel("Statistics:");
		controls.add(statsLabel);

		JButton statsButton = new JButton("Graph");
		statsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				statsUpdater.toggleGraphVisible();
			}

		});

		controls.add(statsButton);




		add(controls, BorderLayout.NORTH);
		expselector.setVisible(true);

		expselector.addActionListener(new ExpSelectorListener());

		loadSimulation(currentexp);

	}

	/**
	 * Sets up known simulation by name
	 * @param expname Experiment name
	 */
	private void loadSimulation(String expname) {
		this.currentexp = expname;


		if (ui != null) {
			ui.killSimulation();
			statsUpdater.dispose();
			remove(ui);
		}


		InputStream datafile = getClass().getResourceAsStream("/experiments/" + experements.get(expname));

		parser = new SimulationConfig(datafile);
		simulation = new Simulation();
		simulation.load(parser);

		ui = new SimulatorUI(simulation);

		add(ui, BorderLayout.CENTER);

		statsUpdater = new LiveStats(ui.getScheduler());
		validate();
		ui.update(true);
	}




}
