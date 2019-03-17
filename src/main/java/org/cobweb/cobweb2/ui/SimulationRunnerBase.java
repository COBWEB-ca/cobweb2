package org.cobweb.cobweb2.ui;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.Simulation;

public class SimulationRunnerBase implements SimulationRunner {

	protected Simulation simulation;

	protected volatile boolean running = false;

	private long tickAutoStop = 0;

	private StatsLogger statsLogger = null;

	private String populationFile = null;

	public SimulationRunnerBase(Simulation simulation) {
		this.simulation = simulation;
	}

	@Override
	public void step() {
		if (isRunning()) {
			stop();
		} else {
			simulation.step();
			updateUI(true);
		}
	}

	@Override
	public void run() {
		if (running)
			throw new IllegalStateException("Already running!");

		running = true;
		notifyStarted();

		System.out.println(String.format(
				"Running '%1$s' for %2$d steps. Log: %3$s",
				simulation.simulationConfig.fileName,
				getAutoStopTime(),
				statsLogger == null ? "No": "Yes"));

		long increment = getAutoStopTime() / 100;
		if (increment > 1000)
			increment = 1000;
		if (increment == 0)
			increment = 1000;

		while(isRunning()) {
			simulation.step();
			updateUI(true);

			long time = getSimulation().getTime();
			if (time % increment == 0) {
				if (getAutoStopTime() != 0) {
					System.out.println(String.format(
							"Step: %1$d / %2$d (%3$d%%)",
							time,
							getAutoStopTime(),
							100 * time /  getAutoStopTime()
							));
				}else {
					System.out.println(String.format(
							"Step: %1$d",
							time
							));
				}
			}

			// Stop at target time
			if (getAutoStopTime() != 0 && simulation.getTime() >= getAutoStopTime()) {
				int saveNum = getSimulation().theEnvironment.getAgentCount();

				if (populationFile != null)
					PopulationSampler.savePopulation(getSimulation(), populationFile, saveNum);
				stop();
			}
		}
		System.out.println("Done!");
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void stop() {
		running = false;

		notifyStopped();
	}

	protected void notifyStarted() {
		for (UpdatableUI updatableUI : new LinkedList<UpdatableUI>(uiComponents)) {
			updatableUI.onStarted();
		}
	}

	protected void notifyStopped() {
		for (UpdatableUI updatableUI : new LinkedList<UpdatableUI>(uiComponents)) {
			updatableUI.onStopped();
		}
	}

	/**
	 * Sets log Writer for the simulation.
	 * null to disable.
	 * @param writer where to write the log.
	 * @see StatsLogger
	 */
	public void setLog(Writer writer) {
		if (statsLogger != null) {
			statsLogger.dispose();
			removeUIComponent(statsLogger);
		}

		if (writer != null) {
			statsLogger = new StatsLogger(writer, simulation);
			addUIComponent(statsLogger);
		}
	}

	public void setPopulationLog(String fileName) {
		this.populationFile = fileName;
	}

	/**
	 * Writes simulation report to writer.
	 * @param writer where to write report.
	 * @see AgentReporter
	 */
	public void report(Writer writer) {
		if (simulation != null) {
			try {
				AgentReporter.report(writer, simulation);
				writer.flush();
				writer.close();
			} catch (IOException ex) {
				throw new UserInputException("Cannot save report file", ex);
			}
		}
	}

	private List<UpdatableUI> uiComponents = new ArrayList<UpdatableUI>();

	protected void updateUI(boolean synchronous) {
		for (UpdatableUI client : new LinkedList<UpdatableUI>(uiComponents)) {
			if (synchronous || client.isReadyToUpdate()) {
				client.update(synchronous);
			}
		}
	}

	@Override
	public void addUIComponent(UpdatableUI ui) {
		uiComponents.add(ui);
		ui.update(true);
	}

	@Override
	public void removeUIComponent(UpdatableUI ui) {
		uiComponents.remove(ui);
	}

	@Override
	public Simulation getSimulation() {
		return simulation;
	}

	public void setAutoStopTime(long t) {
		tickAutoStop = t;
	}

	public long getAutoStopTime() {
		return tickAutoStop;
	}
}
