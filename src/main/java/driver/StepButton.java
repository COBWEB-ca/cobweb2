//[]SK new class
package driver;

import javax.swing.JButton;
import javax.swing.JTextField;

/**
 *This class represents the button responsible for advancing 
 *the application by one time tick.
 *
 * @author skinawy
 */
public class StepButton extends JButton implements
		java.awt.event.ActionListener {
	private cobweb.UIInterface uiPipe;

	public StepButton(cobweb.UIInterface theUI) {
		super("Step");
		uiPipe = theUI;
		addActionListener(this);
	}

	public void setUI(cobweb.UIInterface theUI) {
		uiPipe = theUI;
	}

	// $$$$$$ Modified on Mar 14
	public void actionPerformed(java.awt.event.ActionEvent e) {
		if  (uiPipe.isRunnable()) {
			long stepTime = uiPipe.getCurrentTime();
			stepTime++;
			JTextField textStepTime = uiPipe.getTimeStopField();
			textStepTime.setText(Long.toString(stepTime));

			if (uiPipe.isRunning()) {
				uiPipe.pause();
			} else {
				uiPipe.resume();
			}
		}
	}

	public static final long serialVersionUID = 0xD4B844C0AA5E3991L;
}