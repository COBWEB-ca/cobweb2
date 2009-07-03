package cwcore;

import java.awt.Color;
import java.util.Collection;

import cobweb.Agent;

public class Colorizer {

	@SuppressWarnings("unused")
	private int selectSize;

	private int numColor;

	private cobweb.Agent[] modelArray;

	@SuppressWarnings("unused")
	private int mode;

	@SuppressWarnings("unused")
	private int[] model;

	@SuppressWarnings("unused")
	private int orignumColor;

	private int MAsize;

	/** The column that stores red pixel values in "rgb_pixel". */
	public static final int RED_PIXEL_COLUMN = 0;

	/** The column that stores green pixel values in "rgb_pixel". */
	public static final int GREEN_PIXEL_COLUMN = 1;

	/** The column that stores blue pixel values in "rgb_pixel". */
	public static final int BLUE_PIXEL_COLUMN = 2;

	public Colorizer(int num, int sS, int m) {
		numColor = num;
		selectSize = sS;
		mode = m;
		/*
		 * model = new int[2]; model[0] = 0; model[1] = 1;
		 */

	}

	/* set a model array of agents for other agents to be compared to */
	public void setModelArray(Collection<Agent> agents, cobweb.Agent[] array) {

		modelArray = new cobweb.Agent[array.length];

		MAsize = 0;
		if (numColor > array.length) {
			MAsize = array.length;
		} else {
			MAsize = numColor;
		}

		for (int i = 0; i < MAsize; ++i) {

			modelArray[i] = array[cobweb.globals.notusedRandom.nextInt(array.length)];

		}
	}

	/*
	 * recolor the agents, if mode is = 1 then recolour the agents according to
	 * the current agents else recolor agents according the the initial agents
	 */
	public void reColorAgents(Collection<Agent> agents, int lmode) {

		if (agents.size() == 0)
			return;

		cobweb.Agent[] array = new cobweb.Agent[agents.size()];
		agents.toArray(array);

		if (lmode == 1) {

			setModelArray(agents, array);

		}
	}

	public void colorAgent(cobweb.Agent agent) {
		int[] agent_rgb_colour_values = agent.getGeneticCode()
				.getGeneticColour();
		Color agent_colour = new Color(
				agent_rgb_colour_values[RED_PIXEL_COLUMN],
				agent_rgb_colour_values[GREEN_PIXEL_COLUMN],
				agent_rgb_colour_values[BLUE_PIXEL_COLUMN]);
		agent.setColor(agent_colour);

	}
}
