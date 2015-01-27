package org.cobweb.cobweb2.plugins.stats;

import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Location;


public class AgentStatistics implements Comparable<AgentStatistics> {

	public final int id;
	public final int type;

	public final long birthTick;
	public long deathTick = -1;

	public final int parent1id;
	public final int parent2id;

	public int countSteps;

	public int countTurns;

	private static final int MAX_PATH_HISTORY = 32;
	public List<Location> path = new LinkedList<Location>();

	public int countAgentBumps;

	public int countRockBumps;

	public int sexualPregs;

	public int directChildren;


	public int pdReward = 0;
	public int pdTemptation = 0;
	public int pdSucker = 0;
	public int pdPunishment = 0;


	public int energyGainFoodMine = 0;
	public int energyGainFoodOther = 0;
	public int energyGainFoodAgents = 0;

	public int energyLossMovement = 0;
	public int energyLossReproduction = 0;

	public AgentStatistics(Agent agent, long time, int parent1id, int parent2id) {
		id = agent.id;
		type = agent.getType();
		birthTick = time;
		this.parent1id = parent1id;
		this.parent2id = parent2id;
	}

	public AgentStatistics(Agent agent, long birth, int parent1id) {
		this(agent, birth, parent1id, -1);
	}

	public AgentStatistics(Agent agent, long birth) {
		this(agent, birth, -1);
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

	@Override
	public int compareTo(AgentStatistics o) {
		return this.id - o.id;
	}


}
