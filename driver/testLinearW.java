package driver;

import javax.swing.JFrame;

import cobweb.Controller;
import cobweb.RandomNoGenerator;
import cobweb.globals;

public class testLinearW extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 5291925007816155186L;

	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException {

		globals.random = new RandomNoGenerator(1);

		ControllerFactory.Init("cwcore.GeneticController");
		Controller a = ControllerFactory.createNew(2, 2);
		ControllerFactory.Init("cwcore.LinearWeightsController");
		Controller b = ControllerFactory.createNew(2, 2);

		LinearAIGUI gui = new LinearAIGUI();
		testLinearW x = new testLinearW();

		x.add(gui);
		x.pack();
		x.setVisible(true);

	}
}
