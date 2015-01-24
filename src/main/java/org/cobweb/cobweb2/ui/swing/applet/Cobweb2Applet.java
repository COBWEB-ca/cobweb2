/**
 *
 */
package org.cobweb.cobweb2.ui.swing.applet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.SimulationConfig;
import org.cobweb.cobweb2.ui.ThreadSimulationRunner;
import org.cobweb.cobweb2.ui.swing.LiveStats;
import org.cobweb.cobweb2.ui.swing.SimulatorUI;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;


/**
 * Applet version of COBWEB2
 */
public class Cobweb2Applet extends JApplet { // NO_UCD. Stop UCDetector from labeling as unused class

	private final Map<String, String> experiments = new LinkedHashMap<String, String>();

	private String currentExp = "Baseline 2009";

	private ThreadSimulationRunner simRunner;
	private SimulatorUI ui;

	private LiveStats liveStats;

	private DisplaySettings displaySettings = new DisplaySettings();

	@Override
	public void init() {
		super.init();
		setSize(580,660);

		setLayout(new BorderLayout());

		experiments.put("Baseline 2009", "baseline 2009.xml");
		experiments.put("Boom and Bust", "boom and bust 2 applet.xml");
		experiments.put("Exponential Growth", "Exponential Growth Experiment.xml");
		experiments.put("Central Place", "central place applet.xml");
		experiments.put("Cheaters vs Cooperators", "cheaters vs cooperators.xml");
		currentExp = "Baseline 2009";

		JPanel controls = new JPanel();

		JLabel selectorlabel = new JLabel("Experiment:");
		controls.add(selectorlabel);

		ExperimentSelector expselector = new ExperimentSelector(experiments);
		controls.add(expselector);
		expselector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				String expname = (String) e.getItem();
				loadSimulation(expname);
			}
		});

		JButton resetbutton = new JButton(resetExperiment);
		controls.add(resetbutton);

		JButton statsButton = new JButton(togglePopulationGraph);
		controls.add(statsButton);

		add(controls, BorderLayout.NORTH);

		loadSimulation(currentExp);
	}

	/**
	 * Load simulation XML resource by name in {@link Cobweb2Applet#experiments}
	 * @param expName Experiment name
	 */
	private void loadSimulation(String expName) {
		this.currentExp = expName;

		if (ui != null) {
			simRunner.stop();
			liveStats.dispose();
			remove(ui);
		}

		InputStream datafile = getClass().getResourceAsStream("/experiments/" + experiments.get(expName));

		SimulationConfig parser = new SimulationConfig(datafile);
		try {
			datafile.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		Simulation simulation = new Simulation();
		simulation.load(parser);
		simRunner = new ThreadSimulationRunner(simulation);

		ui = new SimulatorUI(simRunner, displaySettings);

		add(ui, BorderLayout.CENTER);

		liveStats = new LiveStats(simRunner);
		validate();
		ui.update(true);
	}

	/**
	 * Resets current experiment
	 */
	private Action resetExperiment = new AbstractAction("Reset") {
		@Override
		public void actionPerformed(ActionEvent e) {
			loadSimulation(currentExp);
		}
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Toggles display of live population and food graph
	 */
	private Action togglePopulationGraph = new AbstractAction("Population Graph") {
		@Override
		public void actionPerformed(ActionEvent e) {
			liveStats.toggleGraphVisible();
		}
		private static final long serialVersionUID = 1L;
	};


	private static final long serialVersionUID = 3127350835002502812L;
}
