package org.cobweb.cobweb2.ui.swing.components;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JButton;

import org.cobweb.cobweb2.ui.SimulationRunner;

public class PauseButton extends JButton implements java.awt.event.ActionListener {
	private SimulationRunner scheduler;

	public static final long serialVersionUID = 0xE55CC6E3B8B5824DL;

	public PauseButton(SimulationRunner scheduler) {
		super("Start");
		this.scheduler = scheduler;
		addActionListener(this);

		setPreferredSize(new Dimension(63, 26));
	}

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		if (scheduler.isRunning()) {
			scheduler.stop();
		} else {
			scheduler.run();
		}
		repaint();
	}

	public void setScheduler(SimulationRunner scheduler) {
		this.scheduler = scheduler;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		boolean running = scheduler.isRunning();
		if (running != myRunning) {
			myRunning = running;
			if (myRunning) {
				setText("Stop");
			} else {
				setText("Start");
			}
		}
		super.paintComponent(g);
	}

	private boolean myRunning = false;

}