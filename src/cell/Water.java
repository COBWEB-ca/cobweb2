package cell;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import cobweb.Agent;
import cobweb.CellObject;
import cobweb.Direction;
import cobweb.Location;
import cwcore.Food;

/**
 * A water tile on the map.
 * 
 * Properties of water tiles:
 * <ol>
 * <li>Single layer(surface)</li>
 * <li>Waste cannot go on water</li>
 * <li>Water contains some concentration of food (single type)</li>
 * </ol>
 * 
 * @author Daniel Kats
 */
public class Water extends CellObject {

	/**
	 * The maximum concentration of food in a single water tile.
	 */
	private static final int MAX_FOOD_CONC = 1000000;

	/**
	 * Add some food to this water.
	 * @param f Food to add.
	 * @return Whether the food was added.
	 */
	public boolean addFood(Food f) {
		if(food.size() < MAX_FOOD_CONC) {
			return food.add(f);
		} else {
			return false;
		}
	}

	/**
	 * Water is blue.
	 * @return Color of water.
	 */
	public final Color getColor() {
		return this.color;
	}

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
	private ArrayList<Food> food;

	/**
	 * Return an iterator over the food.
	 * @return The food in this blood.
	 */
	public Iterator<Food> getFood() {
		return food.iterator();
	}

	/**
	 * The color of the water.
	 */
	protected Color color;

	/**
	 * Create a new water tile.
	 */
	public Water(Location coords) {
		this.position = coords;
		this.color = Color.blue;
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

	@Override
	public void onStep(Agent a) {
		//FIXME: Implement me
		throw new RuntimeException("FIXME: Implement me");
	}
}
