package org.cobweb.cobweb2.ui.swing.components;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JButton;

import org.cobweb.cobweb2.ui.Scheduler;

public class PauseButton extends JButton implements java.awt.event.ActionListener {
	private Scheduler scheduler;

	public static final long serialVersionUID = 0xE55CC6E3B8B5824DL;

	public PauseButton(Scheduler scheduler) {
		super("Start");
		this.scheduler = scheduler;
		addActionListener(this);

		setPreferredSize(new Dimension(63, 26));
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		if (scheduler.isRunning()) {
			scheduler.pause();
		} else {
			scheduler.resume();
		}
		repaint();
	}

	public void setScheduler(Scheduler scheduler) {
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