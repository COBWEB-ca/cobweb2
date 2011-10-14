package cwcore;

import cobweb.Agent;
import cobweb.CellObject;

/**
 * A seed 
 */
public class Seed extends CellObject {
	/**
	 * The food source that this seed transforms into
	 */
	private FoodSource dormantFoodSource;

	/**
	 * The amount of time this seed will be dormant for.
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
	 * Create a new seed.
	 */
	public Seed(FoodSource f, long sleepLength, long plantedTick) {
		this.sleepLength = sleepLength;
		this.dormantFoodSource = f;
		this.plantedTick = plantedTick;

		this.age = 0;
	}

	/**
	 * Get the tick in which this seed was planted.
	 * @return The tick in which this seed was planted.
	 */
	public long getPlantedTick() {
		return plantedTick;
	}

	/**
	 * Age this seed by 1 tick.
	 */
	public void age() {
		this.age++;
	}

	/**
	 * True if this seed is ready to sprout, false otherwise.
	 * @return True if the seed is ready to sprout, false otherwise.
	 */
	public boolean willSprout() {
		return age > this.sleepLength;
	}

	/**
	 * Return the food source that this seed sprouts into.
	 * If the seed is not ready to sprout, return null.
	 * @return The food source that the seed sprouts into.
	 */
	public FoodSource sprout() {
		if(this.willSprout()) {
			return this.dormantFoodSource;
		} else {
			return null;
		}
	}

	/**
	 * Only agent and waste can go on top.
	 * @return True for agent and waste, false otherwise.
	 */
	@Override
	public boolean canCoverWith(CellObject other) {
		return other instanceof Agent || other instanceof Waste;
	}
}
