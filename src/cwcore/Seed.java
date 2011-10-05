package cwcore;

import cobweb.Environment;

/**
 * A seed 
 */
public class Seed {
	/**
	 * The food source that this seed transforms into
	 */
	private FoodSource dormantFoodSource;

	/**
	 * 
	 */
	private long sleepLength;

	/**
	 * The tick number at which this seed was planted.
	 */
	private long plantedTick;

	/**
	 * The seed's current age.
	 */
	private long age;

	/**
	 * Coordinates of the 
	 */
	private Environment.Location coords;

	/**
	 * Create a new seed.
	 */
	public Seed(FoodSource f, long sleepLength, long plantedTick) {
		this.sleepLength = sleepLength;
		this.dormantFoodSource = f;
		this.plantedTick = plantedTick;
	}

	/**
	 * Get the tick in 
	 * 
	 * @return
	 */
	public long getPlantedTick() {
		return plantedTick;
	}

	/**
	 * Age this seed.
	 */
	public void age() {
		this.age++;
	}

	/**
	 * Return the seed's location.
	 * @return The seed's location.
	 */
	public Environment.Location getLocation() {
		return coords;
	}
}
