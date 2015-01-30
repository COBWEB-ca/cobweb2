package org.cobweb.cobweb2.plugins.vision;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.plugins.AgentState;


public class VisionState implements AgentState {

	private final Environment environment;
	private final Agent agent;

	public VisionState(Environment environment, Agent agent) {
		this.environment = environment;
		this.agent = agent;
	}

	public static final int LOOK_DISTANCE = 4;

	/**
	 * This method allows the agent to see what is in front of it.
	 *
	 * @return What the agent sees and at what distance.
	 */
	public SeeInfo distanceLook() {
		LocationDirection destPos = environment.topology.getAdjacent(agent.getPosition());

		for (int dist = 1; dist <= LOOK_DISTANCE; ++dist) {

			// We are looking at the wall
			if (destPos == null)
				return new SeeInfo(dist, Environment.FLAG_STONE, LOOK_DISTANCE);

			// Check for stone...
			if (environment.hasStone(destPos))
				return new SeeInfo(dist, Environment.FLAG_STONE, LOOK_DISTANCE);

			// If there's another agent there, then return that it's a stone...
			if (environment.hasAgent(destPos) && environment.getAgent(destPos) != agent)
				return new SeeInfo(dist, Environment.FLAG_AGENT, LOOK_DISTANCE);

			// If there's food there, return the food...
			if (environment.hasFood(destPos))
				return new SeeInfo(dist, Environment.FLAG_FOOD, LOOK_DISTANCE);

			if (environment.hasDrop(destPos))
				return new SeeInfo(dist, Environment.FLAG_DROP, LOOK_DISTANCE);

			destPos = environment.topology.getAdjacent(destPos);
		}
		return new SeeInfo(LOOK_DISTANCE);
	}


	@Override
	public boolean isTransient() {
		return true;
	}
	private static final long serialVersionUID = 1L;
}
