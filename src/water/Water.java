package water;

import java.util.ArrayList;

import cobweb.Agent;
import cobweb.CellObject;
import cobweb.Direction;
import cobweb.Environment;
import cwcore.Food;

/**
 * A water tile on the map.
 * 
 * Properties of water tiles:
 * <ol>
 * <li>Single layer(surface)</li>
 * <li>Waste cannot go on water</li>
 * </ol>
 * 
 * @author Daniel Kats
 */
public class Water extends CellObject {

	//we must create a 3D cobweb...

	/**
	 * The speed of the current, which governs how fast this tile moves.
	 */
	private int currentSpeed;

	/**
	 * The direction of the current.
	 */
	private Direction currentDir;

	/**
	 * The food contained in this water tile.
	 */
	ArrayList<Food> food;

	/**
	 * Create a new water tile.
	 */
	public Water(Environment.Location coords) {
		this.position = coords;
	}

	/**
	 * Return the direction of the current.
	 * @return The direction of the current.
	 */
	public Direction getCurrentDirection() {
		return this.currentDir;
	}

	/**
	 * Set the direction of the current to the given direction.
	 * @param dir The direction of the current.
	 */
	public void setCurrentDirection(Direction dir) {
		this.currentDir = dir;
	}

	/**
	 * Return the speed of the current.
	 * @return The speed of the current.
	 */
	public int getCurrentSpeed() {
		return this.currentSpeed;
	}

	/**
	 * Set the current's speed to the given speed.
	 * @param currentSpeed The current speed.
	 */
	public void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	/**
	 * Water can be covered with swimming agents.
	 * Everything else that drops into the water dies. 
	 */
	@Override
	public boolean canCoverWith(CellObject other) {
		return (other instanceof Agent) && (((Agent) other).canSwim());
	}

	/**
	 * Water can move according to the current.
	 */
	@Override
	public boolean canMove() {
		return true;
	}
}
