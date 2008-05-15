//[]SK new class
package driver;

/**
 * 
 * @author skinawy This class represents the button responsible for advancing
 *         the appliaction by one time tick.
 * 
 */
public class StepButton extends java.awt.Button implements
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
		setLabel("Step");
	}
	
	// $$$$$$ Modified on Mar 14
	public void actionPerformed(java.awt.event.ActionEvent e) {
		if  (GUI.frame.isVisible() == false) {
			java.lang.Long stepTime = uiPipe.getTime();
			stepTime++;
			java.awt.TextField textStepTime = uiPipe.getTickField();
			textStepTime.setText(stepTime.toString());
			if (uiPipe.isPaused())
				uiPipe.resume();
			else
				uiPipe.pause();
		}
		/* $$$$$ Original code.  Mar 14
		java.lang.Long stepTime = uiPipe.getTime();
		stepTime++;
		java.awt.TextField textStepTime = uiPipe.getTickField();
		textStepTime.setText(stepTime.toString());
		if (uiPipe.isPaused())
			uiPipe.resume();
		else
			uiPipe.pause();
		*/
	}
}