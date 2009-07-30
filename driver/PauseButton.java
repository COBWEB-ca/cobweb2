package driver;

import java.awt.Dimension;

import javax.swing.JButton;

public class PauseButton extends JButton implements java.awt.event.ActionListener {
	private cobweb.UIInterface uiPipe;

	public PauseButton(cobweb.UIInterface theUI) {
		uiPipe = theUI;
		updateLabel();
		addActionListener(this);

		setPreferredSize(new Dimension(63,26));
	}

	public void setUI(cobweb.UIInterface theUI) {
		uiPipe = theUI;
		updateLabel();
	}

	public void updateLabel() {
		//System.out
		//		.println("-------------- UpdateLabel Called ----------------");  // $$$$$$ Used for testing purpose, silenced on Mar 10
		if (uiPipe.isPaused()) {
			setText("Start");
		} else {
			setText("Stop");
		}
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		if (uiPipe.isRunnable()) {
			if (uiPipe.isPaused()) {
				uiPipe.resume();
			} else {
				uiPipe.pause();
			}
			updateLabel();
		}
	}

	public static final long serialVersionUID = 0xE55CC6E3B8B5824DL;
}