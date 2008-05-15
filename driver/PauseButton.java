package driver;

public class PauseButton extends java.awt.Button implements
		java.awt.event.ActionListener {
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
		if (uiPipe.isPaused())
			setLabel("Resume");
		else
			setLabel("Pause");
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {
		// $$$$$$ Modified on Mar 14
		if (GUI.frame.isVisible() == false) {
			if (uiPipe.isPaused())
				uiPipe.resume();
			else
				uiPipe.pause();
			updateLabel();
		}
		/*  $$$$$ Original code
		if (uiPipe.isPaused())
			uiPipe.resume();
		else
			uiPipe.pause();
		updateLabel();
		*/
	}
}