package org.cobweb.cobweb2.core;

/**
 * This class provides the information of what an agent sees.
 *
 */
public class SeeInfo {
	private int dist;

	private int type;

	/**
	 * Contains the information of what the agent sees.
	 *
	 * @param d Distance to t.
	 * @param t Type of object seen.
	 */
	public SeeInfo(int d, int t) {
		dist = d;
		type = t;
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
}