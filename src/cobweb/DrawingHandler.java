package cobweb;

import java.awt.Color;
import java.util.List;


public interface DrawingHandler {

	/**
	 * Inform the UI of the visual state of an agent.
	 * 
	 * @param agentColor the colour of the agent.
	 * @param typeColor dot color
	 * @param strategyColor colour of agent's outline
	 * @param position the tile position of the agent.
	 * @param facing a direction vector for the facing direction of the agent. A
	 *            facing of (0,0) means the agent has no facing direction.
	 */
	public abstract void newAgent(Color agentColor, Color typeColor, Color strategyColor, Point2D position,
			Point2D facing);


	public abstract void newPath(List<Location> path);

	/**
	 * Inform the UI of a new tile color array.
	 * 
	 * @param tileColors a width * height array of tile colors.
	 * @param width grid width
	 * @param height grid height
	 */
	public abstract void newTileColors(int width, int height, Color[] tileColors);

	public abstract void newDrop(Point2D position, Color color);

}
