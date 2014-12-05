//[]SK new class
package org.cobweb.cobweb2.ui.swing.components;

import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.cobweb.cobweb2.ui.SimulationRunner;

/**
 *This class represents the button responsible for advancing
 *the application by one time tick.
 *
 * @author skinawy
 */
public class StepButton extends JButton implements ActionListener {
	private SimulationRunner  scheduler;

	public StepButton(SimulationRunner scheduler) {
		super("Step");
		setScheduler(scheduler);
		addActionListener(this);
	}

	public void setScheduler(SimulationRunner scheduler) {
		this.scheduler = scheduler;
	}

	// $$$$$$ Modified on Mar 14
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		scheduler.step();
	}

	public static final long serialVersionUID = 0xD4B844C0AA5E3991L;
}