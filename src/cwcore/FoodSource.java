package cwcore;

import java.util.LinkedList;

import cobweb.Environment;

/**
 * A mine of food.
 */
public class FoodSource {
	/**
	 * The number of seeds this food source currently has.
	 */
	private int numSeeds;

	/**
	 * The probability per turn that this food source will reproduce by spreading all its seeds.
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
	 * Spread all seeds. Return the locations where the seeds successfully germinate.
	 */
	private final LinkedList<Environment.Location> spreadSeeds() {
		Environment.Location randomLoc;
		double prob;
		LinkedList<Environment.Location> goodSpots = new LinkedList<Environment.Location>();

		for(int i = 0; i < numSeeds; i++) {		
			randomLoc = this.coords.getEnvironment().getRandomFreeLocation();
			prob = probGerminate(this.coords.distanceSquare(randomLoc));

			if(rollRandom(prob)) {
				goodSpots.add(randomLoc);
			}
		}

		return goodSpots;
	}

	/**
	 * Return the probability that a seed that landed at the given square
	 * distance from this food source will successfully germinate.
	 * @param distanceSq Square distance from this food source.
	 * @return The probability that the seed germinates.
	 */
	public static double probGerminate(int distanceSq) {
		//P(d^2) = e^(-2d^2 / e)
		return Math.pow(Math.E, -2 * distanceSq / Math.E);
	}

	/**
	 * Deplete this food source once.
	 */
	public void deplete() {
		this.foodLeft = (int) Math.round(this.foodLeft * (1 - this.depletionRate));
	}

	/**
	 * Check if the food source reproduces. If it does, return the offspring(s).
	 * If no reproduction takes place, return null.
	 * @return The offspring(s) if successful, empty list on failure.
	 * TODO for now creates exact copies
	 */
	public LinkedList<FoodSource> reproduce() {
		LinkedList<FoodSource> children = new LinkedList<FoodSource>();

		if(rollRandom(this.sporeProb)) {
			LinkedList<Environment.Location> goodSpots = this.spreadSeeds();

			while(!goodSpots.isEmpty()) {
				children.add(new FoodSource(this.startFood, this.type, goodSpots.pop(), this.depletionRate, this.sporeProb));
			}
		} 

		return children;
	}

	/**
	 * Return true if the event with given probability occurred, false otherwise.
	 * TODO move this somewhere more logical
	 * @param prob The probability of the event occurring.
	 * @return True if the event occurred, false otherwise.
	 */
	public static final boolean rollRandom(double prob) {
		if(prob == 0) {
			return false;
		} else {
			return cobweb.globals.random.nextFloat() < prob;
		}
	}

	/**
	 * Return the location of this food source.
	 * @return The location of this food source.
	 */
	public final Environment.Location getLocation() {
		return this.coords;
	}

	/**
	 * Remove a piece of food from the food source.
	 * @return A piece of food that is of the same type as the food source. Null, if no food.
	 */
	public final Food getFood() {
		this.startFood--;

		return new Food(this.type);
	}

	/**
	 * Return true if the food source is depleted, false otherwise.
	 * @return True if food source is empty, false otherwise.
	 */
	public final boolean isEmpty() {
		return this.foodLeft == 0;
	}

	/**
	 * Return the type of food this food source produces.
	 * @return Type of food this food source produces.
	 */
	public final int getType() {
		return this.type;
	}

	/**
	 * @param type Food source type
	 * @author RickyD
	 */
	public final void setType (int type) {
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
