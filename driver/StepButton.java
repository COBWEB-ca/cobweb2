//[]SK new class
package driver;

import javax.swing.JButton;
import javax.swing.JTextField;

/**
 *
 * @author skinawy This class represents the button responsible for advancing
 *         the appliaction by one time tick.
 *
 */
public class StepButton extends JButton implements
		java.awt.event.ActionListener {
	private cobweb.UIInterface uiPipe;

	public StepButton(cobweb.UIInterface theUI) {
		uiPipe = theUI;
		updateLabel();
		addActionListener(this);
	}

	public void setUI(cobweb.UIInterface theUI) {
		uiPipe = theUI;
		updateLabel();
	}

	public void updateLabel() {
		setText("Step");
	}

	// $$$$$$ Modified on Mar 14
	public void actionPerformed(java.awt.event.ActionEvent e) {
		if  (uiPipe.isRunnable()) {
			java.lang.Long stepTime = uiPipe.getTime();
			stepTime++;
			JTextField textStepTime = uiPipe.getTickField();
			textStepTime.setText(stepTime.toString());

			if (uiPipe.isPaused()) {
				uiPipe.resume();
			} else {
				uiPipe.pause();
			}
		}
	}

	public static final long serialVersionUID = 0xD4B844C0AA5E3991L;
}