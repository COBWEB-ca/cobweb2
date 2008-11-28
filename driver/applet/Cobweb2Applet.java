/**
 *
 */
package driver.applet;

import java.io.InputStream;

import javax.swing.JApplet;

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

	@Override
	public void init() {
		super.init();

		setSize(580,660);

		InputStream datafile = getClass().getResourceAsStream("/resources/baseline.xml");

		Parser p = new Parser(datafile);

		SimulatorUI ui = new SimulatorUI(p);
		getContentPane().add(ui);
	}





}
