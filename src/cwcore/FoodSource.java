package cwcore;

/**
 * A mine of food.
 */
public class FoodSource {
	/**
	 * The amount of food left in the food source.
	 */
	private int foodLeft;

	/**
	 * The amount of food this food source had initially.
	 */
	private int startFood;

	/**
	 * The type of food this mine provides.
	 * The type corresponds to the type of agent that gets the most benefit from this food.
	 */
	private int type;

	/**
	 * Create a new food source with the g
	 */
	public FoodSource(int startFood, int type) {
		this.startFood = startFood;
		this.foodLeft = startFood;
		this.type = type;
	}

	/**
	 * Remove a piece of food from the food source.
	 * @return A piece of food that is of the same type as the food source.
	 */
	public Food getFood() {
		this.startFood--;
		return new Food(this.type);
	}

	/**
	 * Return true if the food source is depleted, false otherwise.
	 * @return True if food source is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return this.foodLeft == 0;
	}
}
