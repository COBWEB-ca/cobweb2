package org.cobweb.cobweb2.ui.swing.stats;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.ui.GridStats;
import org.cobweb.cobweb2.ui.GridStats.CellStats;
import org.cobweb.cobweb2.ui.GridStats.RegionOptions;
import org.cobweb.cobweb2.ui.UpdatableUI;


public class RegionLogger implements UpdatableUI {

	private RegionOptions opts;
	private Simulation simulation;
	private PrintWriter logStream;

	public RegionLogger(Writer w, Simulation simulation, RegionOptions opts) {
		this.simulation = simulation;
		this.opts = opts;

		logStream = new java.io.PrintWriter(w, false);
		writeLogTitles();
	}

	private void writeLogTitles() {
		logStream.print("Tick");

		List<String> headers = new ArrayList<>();
		headers.add("All Agents ");
		headers.add("All Food ");


		List<String> cellNames = new ArrayList<>();
		for (int r = 0; r < opts.vDivisions; r++) {
			for (int c = 0; c < opts.hDivisions; c++) {
				cellNames.add("R" + (r+1) + "C" + (c+1));
			}
		}

		for (String cellName : cellNames) {
			logStream.print('\t');
			logStream.print("All Agents " + cellName);
		}

		for (String cellName : cellNames) {
			logStream.print('\t');
			logStream.print("All Food " + cellName);
		}

		for (int i = 0; i < simulation.getAgentTypeCount(); i++) {
			for (String cellName : cellNames) {
				logStream.print('\t');
				logStream.print("Agent " + (i+1) + " " + cellName);
			}
		}

		for (int i = 0; i < simulation.getAgentTypeCount(); i++) {
			for (String cellName : cellNames) {
				logStream.print('\t');
				logStream.print("Food " + (i+1) + " " + cellName);
			}
		}

		logStream.println();
	}

	@Override
	public void update(boolean synchronous) {
		writeLogEntry();
	}

	private void writeLogEntry() {
		GridStats stats = new GridStats(simulation, opts);
		logStream.print(simulation.getTime());

		List<CellStats> cells = new ArrayList<>();
		for (int r = 0; r < opts.vDivisions; r++) {
			for (int c = 0; c < opts.hDivisions; c++) {
				CellStats cellStats = stats.cellStats[c][r];
				cells.add(cellStats);
			}
		}

		for (CellStats cellStats : cells) {
			logStream.print('\t');
			logStream.print(cellStats.totalAgents());
		}

		for (CellStats cellStats : cells) {
			logStream.print('\t');
			logStream.print(cellStats.totalFood());
		}

		for (int i = 0; i < simulation.getAgentTypeCount(); i++) {
			for (CellStats cellStats : cells) {
				logStream.print('\t');
				logStream.print(cellStats.agentCount[i]);
			}
		}

		for (int i = 0; i < simulation.getAgentTypeCount(); i++) {
			for (CellStats cellStats : cells) {
				logStream.print('\t');
				logStream.print(cellStats.foodCount[i]);
			}
		}

		logStream.println();
	}

	@Override
	public boolean isReadyToUpdate() {
		return true;
	}

	public void dispose() {
		logStream.flush();
		logStream.close();
	}

	@Override
	public void onStopped() {
		logStream.flush();
	}

	@Override
	public void onStarted() {
		// TODO Auto-generated method stub

	}

}
