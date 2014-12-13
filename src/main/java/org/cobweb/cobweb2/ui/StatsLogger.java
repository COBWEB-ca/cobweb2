package org.cobweb.cobweb2.ui;

import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;

import org.cobweb.cobweb2.Simulation;


public class StatsLogger implements UpdatableUI {

	private PrintWriter logStream;
	private Simulation simulation;

	public StatsLogger(Writer w, Simulation simulation) {
		this.simulation = simulation;
		logStream = new java.io.PrintWriter(w, false);
		writeLogTitles();
	}


	/**
	 * Write to Log file: FoodCount, AgentCount, Average Agent Energy and Agent
	 * Energy at the most recent ticks ( by tick and by Agent/Food preference)
	 */
	public void writeLogEntry() {
		if (logStream == null) {
			return;
		}

		java.text.DecimalFormat z = new DecimalFormat("#,##0.000");

		for (int i = 0; i < simulation.getAgentTypeCount(); i++) {

			long agentCount = simulation.theEnvironment.countAgents(i);
			long agentEnergy = simulation.theEnvironment.countAgentEnergy(i);
			/*
			 * System.out
			 * .println("************* Near Agent Count *************");
			 */

			int cheaters = (simulation.theEnvironment.numAgentsStrat(i))[1];
			int coops = (simulation.theEnvironment.numAgentsStrat(i))[0];
			logStream.print(simulation.getTime());
			logStream.print('\t');
			logStream.print(simulation.theEnvironment.countFoodTiles(i));
			logStream.print('\t');
			logStream.print(agentCount);
			logStream.print('\t');
			// Format Average agentEnergy to 3 decimal places
			if (agentCount != 0)
				logStream.print(z.format(((float) agentEnergy) / agentCount));
			else
				logStream.print("0.000");
			logStream.print('\t');
			logStream.print(agentEnergy);
			logStream.print('\t');
			logStream.print(cheaters);
			logStream.print('\t');
			logStream.print(coops);
			logStream.print('\t');

			for (String s : simulation.mutatorListener.logDataAgent(i)) {
				logStream.print(s);
				logStream.print('\t');
			}
		}
		// print the TOTAL of FoodCount, AgentCount, Average Agent Energy
		// and Agent Energy at a certain tick
		/*
		 * System.out
		 * .println("************* Before Agent Count Call *************");
		 */
		long agentCountAll = simulation.theEnvironment.getAgentCount();
		/*
		 * System.out
		 * .println("************* After Agent Count Call *************");
		 */
		long agentEnergyAll = simulation.theEnvironment.countAgentEnergy();
		int total_cheaters = (simulation.theEnvironment.numAgentsStrat())[1];
		int total_coops = (simulation.theEnvironment.numAgentsStrat())[0];
		logStream.print(simulation.getTime());
		logStream.print('\t');
		logStream.print(simulation.theEnvironment.countFoodTiles());
		logStream.print('\t');
		logStream.print(agentCountAll);
		logStream.print('\t');
		logStream.print(z.format(((float) agentEnergyAll) / agentCountAll));
		logStream.print('\t');
		logStream.print(agentEnergyAll);
		logStream.print('\t');
		logStream.print(total_cheaters);
		logStream.print('\t');
		logStream.print(total_coops);
		logStream.print('\t');

		for (String s : simulation.mutatorListener.logDataTotal()) {
			logStream.print(s);
			logStream.print('\t');
		}
		logStream.println();
		// logStream.flush();
	}

	/* Write the Log titles to the file,(called by log (java.io.Writer w)) */
	private void writeLogTitles() {
		if (logStream != null) {
			for (int i = 1; i <= simulation.getAgentTypeCount(); i++) {

				logStream.print("Tick\t");
				logStream.print("FoodCount " + i + "\t");
				logStream.print("AgentCount " + i + "\t");
				logStream.print("AveAgentEnergy " + i + "\t");
				logStream.print("AgentEnergy " + i + "\t");
				logStream.print("Cheat " + i + "\t");
				logStream.print("Coop " + i + "\t");
				for (String s : simulation.mutatorListener.logHederAgent()) {
					logStream.print(s);
					logStream.print(" " + i);
					logStream.print('\t');
				}
			}
			// One final round of output for total
			logStream.print("Tick\t");
			logStream.print("FoodCount T\t");
			logStream.print("AgentCount T\t");
			logStream.print("AveAgentEnergy T\t");
			logStream.print("AgentEnergy T\t");
			logStream.print("Num. Cheat T\t");
			logStream.print("Num. Coop T\t");
			for (String s : simulation.mutatorListener.logHederTotal()) {
				logStream.print(s);
				logStream.print(" T");
				logStream.print('\t');
			}
			logStream.println();
		}
	}

	public void dispose() {
		logStream.flush();
		logStream.close();
	}


	@Override
	public void update(boolean synchronous) {
		writeLogEntry();
	}


	@Override
	public boolean isReadyToUpdate() {
		return true;
	}


	@Override
	public void onStopped() {
		logStream.flush();
	}


	@Override
	public void onStarted() {
		// Nothing
	}
}
