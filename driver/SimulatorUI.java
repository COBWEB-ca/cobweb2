/**
 *
 */
package driver;

import java.awt.BorderLayout;
import java.awt.TextField;
import java.awt.event.FocusEvent;
import java.awt.event.TextEvent;

import javax.swing.JPanel;

import cobweb.LocalUIInterface;
import cobweb.Environment.EnvironmentStats;
import cobweb.LocalUIInterface.TickEventListener;

/**
 *
 * JPanel of the main display area of CobwebApplication, contains grid and the pause/stop/stop at controls
 * @author igor
 *
 */
public class SimulatorUI extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 2671092780367865697L;

	private final LocalUIInterface uiPipe;

	public void AddTickEventListener(TickEventListener listener) {
		uiPipe.AddTickEventListener(listener);
	}



	public void RemoveTickEventListener(TickEventListener listener) {
		uiPipe.RemoveTickEventListener(listener);
	}



	private DisplayPanel displayPanel;

	private PauseButton pauseButton;

	private StepButton stepButton;

	public TextField tickField;


	public SimulatorUI(Parser p) {
		uiPipe = new LocalUIInterface(new CobwebUIClient(), p);
		setLayout(new BorderLayout());

		UIsettings(uiPipe);

		this.add(displayPanel);

		
	}

	
	
	public EnvironmentStats getStatistics() {
		return uiPipe.getStatistics();
	}

	public void UIsettings(cobweb.UIInterface uiPipe) {

		uiPipe.setRefreshTimeout(100);
		uiPipe.setFrameSkip(0);
		if (displayPanel == null) {
			displayPanel = new DisplayPanel(uiPipe, 10, 20);
		} else {
			displayPanel.setUI(uiPipe);
		}


		add(displayPanel, "Center");
		if (tickField == null) {
			displayPanel.add(new java.awt.Label("Stop at"));
			tickField = new java.awt.TextField(8);
			displayPanel.add(tickField);
		}

		if (pauseButton == null) {
			pauseButton = new PauseButton(uiPipe);
			displayPanel.add(pauseButton, "North");
			stepButton = new StepButton(uiPipe);
			displayPanel.add(stepButton/* , "North" */);
			MyScrollbar sb = new MyScrollbar(uiPipe);
			displayPanel.add(new java.awt.Label("   Adjust Speed:"));
			displayPanel.add(sb);
		} else {
			pauseButton.setUI(uiPipe);
		}

		if (stepButton == null) {
			pauseButton = new PauseButton(uiPipe);
			displayPanel.add(pauseButton, "North");
			stepButton = new StepButton(uiPipe);
			displayPanel.add(stepButton);
			MyScrollbar sb = new MyScrollbar(uiPipe);
			displayPanel.add(new java.awt.Label("   Adjust Speed:"));
			displayPanel.add(sb);
		} else {
			stepButton.setUI(uiPipe);
		}

		uiPipe.setTickField(tickField);


		// $$$$$$ Implemented specially for Linux to show inputting numbers in the box after "Stop at".  Mar 20.
		tickField.addTextListener(new java.awt.event.TextListener() {
			//@To Override, need delete the following method first, then "Quick fix".
			public void textValueChanged(TextEvent e) {
				tickField.repaint();
			}
		});

		tickField.addFocusListener(new java.awt.event.FocusAdapter(){
			@Override
			public void focusLost(FocusEvent e) {
				tickField.repaint();
			}
			@Override
			public void focusGained(FocusEvent e) {
				tickField.repaint();
			}
		});

		//TODO: use mouse to click on grid

		uiPipe.setPauseButton(pauseButton); // $$$$$$ Mar 20

		validate();
		uiPipe.start();
	} // end of UISettings



	private class CobwebUIClient implements cobweb.UIInterface.UIClient {
		public void refresh(cobweb.UIInterface theUI) {
			displayPanel.repaint();
			pauseButton.repaint();
			stepButton.repaint();
		}
	}
}
