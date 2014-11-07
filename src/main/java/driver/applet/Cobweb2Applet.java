/**
 *
 */
package driver.applet;

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

import driver.LiveStats;
import driver.SimulationConfig;
import driver.SimulatorUI;


/**
 * Applet version of COBWEB2
 */
public class Cobweb2Applet extends JApplet {

	private class ExpSelectorListener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
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
			public void actionPerformed(ActionEvent e) {
				loadSimulation(currentexp);
			}
		});

		statsLabel = new JLabel("Statistics:");
		controls.add(statsLabel);

		JButton statsButton = new JButton("Graph");
		statsButton.addActionListener(new ActionListener() {
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
			ui.RemoveTickEventListener(statsUpdater);
			remove(ui);
		}


		InputStream datafile = getClass().getResourceAsStream("/experiments/" + experements.get(expname));

		parser = new SimulationConfig(datafile);
		ui = new SimulatorUI(parser);

		//FIX: DisplayPanel is buggy, so we have to hide and show it for it to redraw
		ui.setVisible(false);
		add(ui, BorderLayout.CENTER);

		ui.setVisible(true);
		statsUpdater = new LiveStats(ui.getUIPipe());
	}




}
