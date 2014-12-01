package org.cobweb.cobweb2.ui.swing.components;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JButton;

public class PauseButton extends JButton implements java.awt.event.ActionListener {
	private org.cobweb.cobweb2.core.UIInterface uiPipe;

	public static final long serialVersionUID = 0xE55CC6E3B8B5824DL;

	public PauseButton(org.cobweb.cobweb2.core.UIInterface theUI) {
		super("Start");
		uiPipe = theUI;
		addActionListener(this);

		setPreferredSize(new Dimension(63, 26));
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		if (uiPipe.isRunnable()) {
			if (uiPipe.isRunning()) {
				uiPipe.pause();
			} else {
				uiPipe.resume();
			}
			repaint();
		}
	}

	public void setUI(org.cobweb.cobweb2.core.UIInterface theUI) {
		uiPipe = theUI;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		boolean running = uiPipe.isRunning();
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