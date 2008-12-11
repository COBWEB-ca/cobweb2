/**
 *
 */
package driver.applet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import driver.Parser;
import driver.SimulatorUI;


/**
 * Applet version of COBWEB2
 * @author igor
 *
 */
public class Cobweb2Applet extends JApplet {

	/**
	 *
	 */
	private static final long serialVersionUID = 3127350835002502812L;
	String asdf;

	Map<String, String> experements = new HashMap<String, String>();


	SimulatorUI ui;
	Parser parser;
	String currentexp;
	ExperementSelector expselector;


	@Override
	public void init() {
		super.init();
		setSize(580,660);
		getContentPane().setLayout(new BorderLayout());

		experements.put("Baseline", "baseline 2008.xml");
		experements.put("Boom and Bust", "boom and bust 2.xml");
		experements.put("Exponential Growth", "Exponential Growth Experiment.xml");
		experements.put("Central Place", "central place 1.xml");
		experements.put("Cheaters vs Cooperators", "cheaters vs cooperators.xml");
		currentexp = "Baseline";




		loadSimulation(currentexp);

		JPanel controls = new JPanel();

		expselector = new ExperementSelector(experements);
		controls.add(expselector);

		getContentPane().add(controls, BorderLayout.SOUTH);
		expselector.setVisible(true);

		expselector.addActionListener(new ExpSelectorListener());

	}

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
	 * @param currentexp
	 */
	private void loadSimulation(String currentexp) {
		if (ui != null) {
			getContentPane().remove(ui);
		}

		InputStream datafile = getClass().getResourceAsStream("/resources/" + experements.get(currentexp));

		parser = new Parser(datafile);
		ui = new SimulatorUI(parser);
		getContentPane().add(ui, BorderLayout.CENTER);
	}





}
