/**
 *
 */
package org.cobweb.cobweb2.ui.swing.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JFormattedTextField;

public class SeedRandomListener implements ActionListener {
	private final JFormattedTextField box;
	private Random random = new Random();

	public SeedRandomListener(JFormattedTextField box) {
		this.box = box;
	}
	public void actionPerformed(ActionEvent e) {
		box.setValue(new Long(Math.abs(random.nextLong() % 100000l)));
	}
}