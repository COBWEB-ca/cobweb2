package org.cobweb.cobweb2.ui.swing;

import java.io.FileWriter;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.ui.AgentReporter;
import org.cobweb.cobweb2.ui.StatsLogger;
import org.cobweb.cobweb2.ui.TickScheduler;

public class SimulationRunner {

	private StatsLogger statsLogger = null;
	protected Simulation simulation;
	protected TickScheduler scheduler;

	/**
	 * Provides the user interface pipe with the location of the log file to 
	 * write to.
	 * 
	 * @param filePath The file path and file name.
	 */
	public void logFile(String filePath) {
		if (simulation != null) {
			if (statsLogger != null) {
				statsLogger.dispose();
				scheduler.removeUIComponent(statsLogger);
			}
			try {
				statsLogger = new StatsLogger(new FileWriter(filePath), simulation);
				scheduler.addUIComponent(statsLogger);
			} catch (Exception ex) {
				throw new CobwebUserException("Cannot save log file!", ex);
			}
		}
	}

	/**
	 * @param filePath file path and file name
	 */
	public void reportFile(String filePath) {
		if (simulation != null) {
			try {
				FileWriter fileWriter = new FileWriter(filePath);
				AgentReporter.report(fileWriter, simulation);
				fileWriter.flush();
				fileWriter.close();
			} catch (Exception ex) {
				throw new CobwebUserException("Cannot save report file", ex);
			}
		}
	}

	/**
	 * Connects a user interface (simulation) to the graphical interface of the 
	 * Cobweb application (uiPipe).
	 * 
	 * @param simulation The user interface used for the simulation.
	 */
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
		if (scheduler != null) {
			scheduler.dispose();
		}
		scheduler = new TickScheduler(simulation);
	}

	public Simulation getSimulation() {
		return simulation;
	}

}
