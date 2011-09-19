package cwcore;

/*
 * Note that there are no setter methods.
 * It type and Location can only be set through the constructor.
 */

/**
 * A chunk of food.
 */
public class Food {
	/**
	 * The type of the food.
	 * Integer value is the agent that best corresponds to the food.
	 */
	private int type;

	/**
	 * Create a new piece of food.
	 */
	public Food(int type) {
		this.type = type;
	}

	/**
	 * Return the type for this food.
	 */
	public int getType() {
		return type;
	}
}
