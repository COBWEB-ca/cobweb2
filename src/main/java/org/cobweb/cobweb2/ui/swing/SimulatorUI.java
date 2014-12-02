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

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.ui.Scheduler;
import org.cobweb.cobweb2.ui.TickScheduler;
import org.cobweb.cobweb2.ui.UpdatableUI;
import org.cobweb.cobweb2.ui.swing.components.PauseButton;
import org.cobweb.cobweb2.ui.swing.components.SpeedBar;
import org.cobweb.cobweb2.ui.swing.components.StepButton;

/**
 *
 * JPanel of the main display area of CobwebApplication, contains grid and the pause/stop/stop at controls
 * @author igor
 *
 */
public class SimulatorUI extends JPanel implements UpdatableUI {
	private static final long serialVersionUID = 2671092780367865697L;

	private final Simulation simulation;

	private DisplayPanel displayPanel;

	private PauseButton pauseButton;

	private StepButton stepButton;

	public JTextField tickField;

	public JLabel tickDisplay;

	private TickScheduler scheduler;

	public SimulatorUI(Simulation sim) {
		simulation = sim;
		scheduler = new TickScheduler(simulation);

		setLayout(new BorderLayout());
		setupUI();

		scheduler.startIdle();
	}

	@Override
	public boolean isReadyToRefresh() {
		return displayPanel != null && displayPanel.isReadyToRefresh();
	}

	@Override
	public void update(boolean sync) {
		if (displayPanel != null) {
			displayPanel.refresh(sync);
		}
	}

	public void setupUI() {

		setLayout(new BorderLayout());

		JPanel controls = new JPanel();

		scheduler.setFrameSkip(0);
		if (displayPanel == null) {
			displayPanel = new DisplayPanel(simulation);
		} else {
			displayPanel.setSimulation(simulation);
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
			pauseButton = new PauseButton(scheduler);
			controls.add(pauseButton);
			stepButton = new StepButton(scheduler);
			controls.add(stepButton);
			controls.add(new JLabel(" Speed:"));
			SpeedBar sb = new SpeedBar(scheduler);
			controls.add(sb);
		} else {
			pauseButton.setScheduler(scheduler);
		}

		if (stepButton == null) {
			pauseButton = new PauseButton(scheduler);
			controls.add(pauseButton);
			stepButton = new StepButton(scheduler);
			controls.add(stepButton);
			controls.add(new JLabel("   Adjust Speed:"));
			SpeedBar sb = new SpeedBar(scheduler);
			controls.add(sb);
		} else {
			stepButton.setScheduler(scheduler);
		}

		// TODO simulation.setTimeStopField(tickField);

		scheduler.addUIComponent(new UpdatableUI() {
			@Override
			public void update(boolean synchronous) {
				tickDisplay.setText("Tick: " + Long.toString(simulation.getTime()) + "  ");
			}

			@Override
			public boolean isReadyToRefresh() {
				return true;
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

		// TODO simulation.setPauseButton(pauseButton);

		validate();
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void killSimulation() {
		scheduler.dispose();
	}
}
