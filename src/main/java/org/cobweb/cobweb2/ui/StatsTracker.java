package org.cobweb.cobweb2.ui;

import java.util.List;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;

public class StatsTracker  {

	protected Simulation simulation;

	public StatsTracker(Simulation simulation) {
		this.simulation = simulation;
	}

	public long countAgentEnergy() {
		long totalEnergy = 0;
		for(Agent a : simulation.theEnvironment.getAgents()){
			ComplexAgent agent = (ComplexAgent) a;
			totalEnergy += agent.getEnergy();
		}
		return totalEnergy;
	}

	public long getAgentCount() {
		return simulation.theEnvironment.getAgentCount();
	}

	public long countAgents(int agentType) {
		long count = 0;
		for(Agent a : simulation.theEnvironment.getAgents()) {
			if (a.getType() == agentType)
				count++;
		}
		return count;
	}

	public long countAgentEnergy(int agentType) {
		long totalEnergy = 0;
		for(Agent a : simulation.theEnvironment.getAgents()) {
			ComplexAgent agent = (ComplexAgent) a;
			if (agent.getType() == agentType)
				totalEnergy += agent.getEnergy();
		}
		return totalEnergy;
	}

	public int[] numAgentsStrat() {
		int stratArray[] = new int[2];
		int cheaters = 0;
		int coops = 0;
		for(Agent a : simulation.theEnvironment.getAgents()) {
			ComplexAgent agent = (ComplexAgent) a;
			if (!agent.getAgentPDActionCheat()) {
				coops++;
				stratArray[0] = coops;
			} else if (agent.getAgentPDActionCheat()) {
				cheaters++;
				stratArray[1] = cheaters;
			}

		}
		return stratArray;
	}

	public int[] numAgentsStrat(int agentType) {
		int stratArray[] = new int[2];
		int cheaters = 0;
		int coops = 0;
		for(Agent a : simulation.theEnvironment.getAgents()) {
			ComplexAgent agent = (ComplexAgent) a;
			if (agent.getType() == agentType && !agent.getAgentPDActionCheat()) {
				coops++;
				stratArray[0] = coops;
			} else if (agent.getType() == agentType && agent.getAgentPDActionCheat()) {
				cheaters++;
				stratArray[1] = cheaters;
			}

		}
		return stratArray;
	}

	public int getAgentTypeCount() {
		return simulation.getAgentTypeCount();
	}

	public long getTime() {
		return simulation.getTime();
	}

	public long countFoodTiles() {
		Environment e = simulation.theEnvironment;
		long foodCount = 0;
		for (int x = 0; x < e.topology.width; ++x) {
			for (int y = 0; y < e.topology.height; ++y) {
				Location currentPos = new Location(x, y);
				if (e.hasFood(currentPos))
					++foodCount;
			}
		}
		return foodCount;
	}

	public int countFoodTiles(int foodType) {
		Environment e = simulation.theEnvironment;
		int foodCount = 0;
		for (int x = 0; x < e.topology.width; ++x) {
			for (int y = 0; y < e.topology.height; ++y) {
				Location currentPos = new Location(x, y);
				if (e.hasFood(currentPos))
					if (e.getFoodType(currentPos) == foodType)
						++foodCount;
			}
		}
		return foodCount;
	}

	public List<String> pluginStatsHeaderAgent() {
		return simulation.mutatorListener.logHeaderAgent();
	}

	public List<String> pluginStatsHeaderTotal() {
		return simulation.mutatorListener.logHeaderTotal();
	}

	public List<String> pluginStatsTotal() {
		return simulation.mutatorListener.logDataTotal();
	}

	public List<String> pluginStatsAgent(int type) {
		return simulation.mutatorListener.logDataAgent(type);
	}

}
