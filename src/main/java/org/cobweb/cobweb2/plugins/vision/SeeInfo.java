package org.cobweb.cobweb2.plugins.vision;

/**
 * This class provides the information of what an agent sees.
 *
 */
public class SeeInfo {
	private final int dist;

	private final int type;

	private final int maxDistance;

	/**
	 * Contains the information of what the agent sees.
	 *
	 * @param d Distance to t.
	 * @param t Type of object seen.
	 * @param maxd Maximum distance the agent is able to see.
	 */
	public SeeInfo(int d, int t, int maxd) {
		maxDistance = maxd;
		dist = d;
		type = t;
	}

	/**
	 * Agent sees nothing.
	 * @param maxd Maximum distance the agent can see
	 */
	public SeeInfo(int maxd) {
		this(maxd, 0, maxd);
	}

	/**
	 * @return How far away the object is.
	 */
	public int getDist() {
		return dist;
	}

	/**
	 * @return What the agent sees (rock, food, etc.)
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return Maximum distance the agent can see
	 */
	public int getMaxDistance() {
		return maxDistance;
	}
}