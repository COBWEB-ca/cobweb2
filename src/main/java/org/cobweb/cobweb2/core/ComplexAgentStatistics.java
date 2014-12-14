package org.cobweb.cobweb2.core;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ComplexAgentStatistics implements Serializable {

	public static final int MAX_PATH_HISTORY = 32;

	public int pdReward = 0;
	public int pdTemptation = 0;
	public int pdSucker = 0;
	public int pdPunishment = 0;

	public int parent1 = -1;

	public int parent2 = -1;

	public long birthTick = -1;

	public long deathTick = -1;

	public int countSteps;

	public int countTurns;

	private List<Location> path = new LinkedList<Location>();

	public int countAgentBumps;

	public int countRockBumps;

	public int agentNumber;

	public int type;

	public int sexualPregs;

	public int directChildren;

	public int energyGainFoodMine = 0;
	public int energyGainFoodOther = 0;
	public int energyGainFoodAgents = 0;

	public int energyLossMovement = 0;
	public int energyLossReproduction = 0;

	public ComplexAgentStatistics(int num, int type, long birth, ComplexAgentStatistics p1, ComplexAgentStatistics p2) {
		this.type = type;
		agentNumber = num;
		birthTick = birth;
		parent1 = p1 != null ? p1.agentNumber : -1;
		parent2 = p2 != null ? p2.agentNumber : -1;
	}

	public ComplexAgentStatistics(int num, int type, long birth, ComplexAgentStatistics p1) {
		this.type = type;
		agentNumber = num;
		birthTick = birth;
		parent1 = p1 != null ? p1.agentNumber : -1;
	}

	public ComplexAgentStatistics(int num, int type, long birth) {
		this.type = type;
		agentNumber = num;
		birthTick = birth;
	}

	/** Adds to the total energy gained from eating other agents. */
	public void addCannibalism(int val) {
		energyGainFoodAgents += val;
	}

	public void addDirectChild() {
		directChildren++;
	}

	public void addFoodEnergy(int val) {
		energyGainFoodMine += val;
	}

	/** Other sources including energy gained from the agent strategy */
	public void addOthers(int val) {
		energyGainFoodOther += val;
	}

	public int boundNorth = Integer.MAX_VALUE;
	public int boundSouth = Integer.MIN_VALUE;
	public int boundWest = Integer.MAX_VALUE;
	public int boundEast = Integer.MIN_VALUE;

	/**
	 * Adds the path information of the agent.
	 *
	 * @param loc Location the agent moved to
	 */
	public void addPathStep(Location loc) {
		if (loc.x < boundWest)
			boundWest = loc.x;
		if (loc.x > boundEast)
			boundEast = loc.x;
		if (loc.y > boundSouth)
			boundSouth = loc.y;
		if (loc.y < boundNorth)
			boundNorth = loc.y;

		path.add(loc);
		if (path.size() > MAX_PATH_HISTORY) {
			path.remove(0);
		}
	}

	public void addSexPreg() {
		sexualPregs++;
	}

	public void addTurn(int cost) {
		countTurns++;
		energyLossMovement += cost;
	}

	public int getAgentNumber() {
		return agentNumber;
	}

	public int getAgentType() {
		return type;
	}

	public long getDeathTick() {
		return deathTick;
	}

	public List<Location> getPathHistory() {
		return path;
	}

	public void setDeath(long death) {
		deathTick = death;
		path = null;
	}

	public void addPDReward() {
		pdReward++;
	}

	public void addPDTemptation() {
		pdTemptation++;
	}

	public void addPDSucker() {
		pdSucker++;
	}

	public void addPDPunishment() {
		pdPunishment++;
	}

	public void useAgentBumpEnergy(int val) {
		countAgentBumps++;
		energyLossMovement += val;
	}

	public void useRockBumpEnergy(int val) {
		countRockBumps++;
		energyLossMovement += val;
	}

	public void useStepEnergy(int val) {
		countSteps++;
		energyLossMovement += val;
	}

	public void useReproductionEnergy(int cost) {
		energyLossReproduction += cost;
	}

	private static final long serialVersionUID = 1L;
}
