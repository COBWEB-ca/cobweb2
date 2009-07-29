/**
 * 
 */
package driver.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JFormattedTextField;

public class SeedRandomListener implements ActionListener {
	private final JFormattedTextField box;
	public SeedRandomListener(JFormattedTextField box) {
		this.box = box;
	}
	public void actionPerformed(ActionEvent e) {
		box.setValue(Math.abs(new Random().nextLong() % 9999l));
	}
}