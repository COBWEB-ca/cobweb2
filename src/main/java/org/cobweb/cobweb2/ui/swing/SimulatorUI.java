/**
 *
 */
package org.cobweb.cobweb2.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cobweb.cobweb2.core.LocalUIInterface;
import org.cobweb.cobweb2.core.UIInterface;
import org.cobweb.cobweb2.core.LocalUIInterface.TickEventListener;
import org.cobweb.cobweb2.core.UIInterface.UIClient;

/**
 *
 * JPanel of the main display area of CobwebApplication, contains grid and the pause/stop/stop at controls
 * @author igor
 *
 */
public class SimulatorUI extends JPanel implements UIClient {
	private static final long serialVersionUID = 2671092780367865697L;

	private final LocalUIInterface uiPipe;

	private DisplayPanel displayPanel;

	private PauseButton pauseButton;

	private StepButton stepButton;

	public JTextField tickField;

	public JLabel tickDisplay;

	public SimulatorUI(SimulationConfig p) {
		uiPipe = new LocalUIInterface(this);
		uiPipe.load(p);
		setLayout(new BorderLayout());

		setupUI();
	}

	public UIInterface getUIPipe() {
		return uiPipe;
	}

	public void AddTickEventListener(TickEventListener listener) {
		uiPipe.AddTickEventListener(listener);
		uiPipe.setRunnable(true);
	}

	public boolean isReadyToRefresh() {
		return displayPanel != null && displayPanel.isReadyToRefresh();
	}

	public void refresh(boolean wait) {
		if (displayPanel != null) {
			displayPanel.refresh(wait);
		}
	}

	public void RemoveTickEventListener(TickEventListener listener) {
		uiPipe.setRunnable(false);
		uiPipe.RemoveTickEventListener(listener);
	}

	public void setupUI() {

		setLayout(new BorderLayout());

		JPanel controls = new JPanel();

		uiPipe.setFrameSkip(0);
		if (displayPanel == null) {
			displayPanel = new DisplayPanel(uiPipe);
		} else {
			displayPanel.setUI(uiPipe);
		}

		add(controls, BorderLayout.NORTH);
		add(displayPanel, BorderLayout.CENTER);

		if (tickDisplay == null) {
			tickDisplay = new JLabel();

			controls.add(tickDisplay);
			tickDisplay.setPreferredSize(new Dimension(90, 20));
		}
		if (tickField == null) {
			controls.add(new JLabel("Stop at"));
			tickField = new JTextField(6);

			controls.add(tickField);

			tickField.setMinimumSize(new Dimension(20, 20));
			tickField.setPreferredSize(new Dimension(20, 20));
		}

		if (pauseButton == null) {
			pauseButton = new PauseButton(uiPipe);
			controls.add(pauseButton);
			stepButton = new StepButton(uiPipe);
			controls.add(stepButton);
			controls.add(new JLabel(" Speed:"));
			SpeedBar sb = new SpeedBar(uiPipe);
			controls.add(sb);
		} else {
			pauseButton.setUI(uiPipe);
		}

		if (stepButton == null) {
			pauseButton = new PauseButton(uiPipe);
			controls.add(pauseButton);
			stepButton = new StepButton(uiPipe);
			controls.add(stepButton);
			controls.add(new JLabel("   Adjust Speed:"));
			SpeedBar sb = new SpeedBar(uiPipe);
			controls.add(sb);
		} else {
			stepButton.setUI(uiPipe);
		}

		uiPipe.setTimeStopField(tickField);

		AddTickEventListener(new TickEventListener() {

			public void TickPerformed(long currentTick) {
				tickDisplay.setText("Tick: " + Long.toString(currentTick) + "  ");
			}
		});

		tickField.addFocusListener(new java.awt.event.FocusAdapter(){
			@Override
			public void focusGained(FocusEvent e) {
				tickField.repaint();
			}
			@Override
			public void focusLost(FocusEvent e) {
				tickField.repaint();
			}
		});

		uiPipe.setPauseButton(pauseButton);

		validate();
		uiPipe.start();
	}

	public void killSimulation() {
		this.uiPipe.killScheduler();
	}

	@Override
	public void setCurrentFile(String input) {
		return;
	}

	@Override
	public void fileOpened(SimulationConfig conf) {
		return;
	}

	@Override
	public void setSimulation(UIInterface simulation) {
		return;
	}
}
