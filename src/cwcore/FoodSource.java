package cwcore;

import cobweb.Direction;
import cobweb.Environment;

/**
 * A mine of food.
 */
public class FoodSource {
	/**
	 * The probability per turn that this food source will reproduce.
	 */
	private double sporeProb;

	/**
	 * The depletion rate of the food.
	 * Every turn, the food this food source contains will be governed by:
	 * F_now = F_last * (1 - depletionRate)
	 */
	private double depletionRate; 

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
	 * Location of this food source.
	 */
	private Environment.Location coords;

	/**
	 * Create a new food source with the given amount of starting food,
	 */
	public FoodSource(int startFood, int type, Environment.Location location, double depleteRate,
			double reproductionProb) {
		this.startFood = startFood;
		this.foodLeft = startFood;
		this.type = type;

		this.depletionRate = depleteRate;
		this.sporeProb = reproductionProb;

		this.coords = location;
	}

	/**
	 * Deplete this food source once.
	 */
	public void deplete() {
		this.foodLeft = (int) Math.round(this.foodLeft * (1 - this.depletionRate));
	}

	/**
	 * Check if the food source reproduces. If it does, return the offspring.
	 * If no reproduction takes place, return null.
	 * @return The offspring if successful, null on failure.
	 * TODO for now creates exact copies
	 */
	public FoodSource reproduce() {
		if(this.rollRandom(this.sporeProb)) {
			//get a random direction
			Direction randDir;
			Environment.Location newCoords;

			//the spawn point for the offspring is adjacent to the position of this food source
			//in the random direction
			do {
				randDir = Direction.getRandom();
				newCoords = this.coords.add(1, randDir);
			} while (newCoords == null);

			return new FoodSource(this.startFood, this.type, newCoords, this.depletionRate, this.sporeProb);
		} else {
			return null;
		}
	}

	/**
	 * Return true if the event with given probability occurred, false otherwise.
	 * @param prob The probability of the event occurring.
	 * @return True if the event occurred, false otherwise.
	 */
	public boolean rollRandom(double prob) {
		if(prob == 0) {
			return false;
		} else {
			return Math.random() < prob;
		}
	}

	/**
	 * Return the location of this food source.
	 * @return The location of this food source.
	 */
	public Environment.Location getLocation() {
		return this.coords;
	}

	/**
	 * Remove a piece of food from the food source.
	 * @return A piece of food that is of the same type as the food source. Null, if no food.
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

	/**
	 * Return the type of food this food source produces.
	 * @return Type of food this food source produces.
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * @param type Food source type
	 * @author RickyD
	 */
	public void setType (int type) {
		this.type = type;
	}

	/**
	 * Compare two food sources. Return true if they are the same food sources, false otherwise.
	 * @param other Another (or the same) food source.
	 * @return True if they are the same food source, false otherwise.
	 */
	public boolean equals(FoodSource other) {
		//assume that only one food source can occupy the same set of coordinates
		return this.getLocation() == other.getLocation();
	}
}
