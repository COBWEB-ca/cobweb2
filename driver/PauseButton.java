package driver;

import javax.swing.JButton;

public class PauseButton extends JButton implements java.awt.event.ActionListener {
	private cobweb.UIInterface uiPipe;

	public PauseButton(cobweb.UIInterface theUI) {
		uiPipe = theUI;
		updateLabel();
		addActionListener(this);
	}

	public void setUI(cobweb.UIInterface theUI) {
		uiPipe = theUI;
		updateLabel();
	}

	public void updateLabel() {
		//System.out
		//		.println("-------------- UpdateLabel Called ----------------");  // $$$$$$ Used for testing purpose, silenced on Mar 10
		if (uiPipe.isPaused()) {
			setText("Resume");
		} else {
			setText("Pause");
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