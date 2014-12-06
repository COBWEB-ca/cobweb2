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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cobweb.cobweb2.ui.ThreadSimulationRunner;
import org.cobweb.cobweb2.ui.UpdatableUI;
import org.cobweb.cobweb2.ui.swing.components.PauseButton;
import org.cobweb.cobweb2.ui.swing.components.SpeedBar;
import org.cobweb.cobweb2.ui.swing.components.StepButton;

/**
 *
 * JPanel of the main display area of CobwebApplication, contains grid and the pause/stop/stop at controls
 *
 */
public class SimulatorUI extends JPanel implements UpdatableUI {
	private DisplayPanel displayPanel;

	private PauseButton pauseButton;

	private StepButton stepButton;

	public JTextField tickField;

	public JLabel tickDisplay;

	private ThreadSimulationRunner simRunner;

	public SimulatorUI(ThreadSimulationRunner runner) {
		simRunner = runner;

		setLayout(new BorderLayout());

		JPanel controls = new JPanel();

		displayPanel = new DisplayPanel(simRunner.getSimulation());

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

			tickField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					update();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					update();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					update();
				}

				private void update() {
					try {
						int time = Integer.parseInt(tickField.getText());
						simRunner.setAutoStopTime(time);
					} catch(NumberFormatException ex) {
						// Nothing
					}
				}
			});
		}

		if (pauseButton == null) {
			pauseButton = new PauseButton(simRunner);
			controls.add(pauseButton);
			stepButton = new StepButton(simRunner);
			controls.add(stepButton);
			controls.add(new JLabel(" Speed:"));
			SpeedBar sb = new SpeedBar(simRunner);
			controls.add(sb);
		} else {
			pauseButton.setScheduler(simRunner);
		}

		if (stepButton == null) {
			pauseButton = new PauseButton(simRunner);
			controls.add(pauseButton);
			stepButton = new StepButton(simRunner);
			controls.add(stepButton);
			controls.add(new JLabel("   Adjust Speed:"));
			SpeedBar sb = new SpeedBar(simRunner);
			controls.add(sb);
		} else {
			stepButton.setScheduler(simRunner);
		}

		simRunner.addUIComponent(this);

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

		update(true);
		validate();
	}

	@Override
	public boolean isReadyToUpdate() {
		return displayPanel != null && displayPanel.isReadyToRefresh();
	}

	@Override
	public void update(boolean sync) {
		tickDisplay.setText("Tick: " + Long.toString(simRunner.getSimulation().getTime()) + "  ");
		if (displayPanel != null) {
			displayPanel.refresh(sync);
		}
	}

	@Override
	public void onStopped() {
		pauseButton.repaint();
		update(true);
	}

	@Override
	public void onStarted() {
		pauseButton.repaint();
	}

	private static final long serialVersionUID = 1L;
}
