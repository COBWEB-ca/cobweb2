package cwcore;

import java.util.LinkedList;

import cobweb.Agent;
import cobweb.CellObject;
import cobweb.Environment;

/**
 * A mine of food.
 */
public class FoodSource extends CellObject {
	/**
	 * The number of seeds this food source currently has.
	 * TODO read this from XML file
	 */
	private final int numSeeds = 10;

	/**
	 * The maximum age of this food source.
	 * TODO read this from XML file
	 */
	private final int maxAge = 40;

	/**
	 * The probability per turn that this food source will reproduce by spreading all its seeds.
	 */
	private double sporeProb;

	/**
	 * The depletion rate of the food.
	 * Every turn, the food this food source contains will be governed by:
	 * F_now = F_last * (1 - depletionRate)
	 * TODO not read anymore
	 */
	private double depletionRate; 

	/**
	 * Maximum food that this food source can contain at a given tick.
	 */
	private int maxFood;

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
	 * The environment in which this food source is contained.
	 */
	private Environment environment;

	/**
	 * The age of this food source.
	 */
	private int age;

	/**
	 * Create a new food source with the given amount of starting food,
	 */
	public FoodSource(int startFood, int type, Environment.Location location, double depleteRate,
			double reproductionProb) {
		this.startFood = startFood;
		this.age = 0;

		this.maxFood = startFood;

		this.foodLeft = startFood;

		this.type = type;

		this.depletionRate = depleteRate;
		this.sporeProb = reproductionProb;

		this.position = location;
		this.environment = this.position.getEnvironment();
	}

	public void age() {
		this.age++;
	}

	/**
	 * Return the number of seeds this food source produces.
	 * @return The number of seeds this food source produces.
	 */
	public int getNumSeeds() {
		return this.numSeeds;
	}

	/**
	 * Spread all seeds. Return the locations where the seeds successfully germinate.
	 */
	private final LinkedList<Environment.Location> spreadSeeds() {
		LinkedList<Environment.Location> goodSpots = new LinkedList<Environment.Location>();
		Environment.Location landingSite;

		for(int i = 0; i < this.numSeeds; i++) {
			//get the seed landing spot
			landingSite = this.getRandomSeedLandingSite();

			//if the seed germinates
			if(rollRandom(probGerminate(landingSite))) {
				goodSpots.add(landingSite);
			}
		}

		return goodSpots;
	}

	/**
	 * Return the probability that a seed that landed at the given square will successfully germinate.
	 * If the seed landed on a non-empty square, the probability is 0.
	 * If the square is empty, the probability decays exponentially away from this square.
	 * @param seedLandingSite The tile on which the seed landed.
	 * @return The probability that the seed germinates.
	 */
	private double probGerminate(Environment.Location seedLandingSite) {
		if(!seedLandingSite.isEmpty()) {
			return 0;
		} 

		int distanceSq = this.position.distanceSquare(seedLandingSite);

		//P(d^2) = e^(-2d^2 / e)
		return Math.pow(Math.E, -2 * Math.sqrt(distanceSq) / Math.E);
	}

	/**
	 * Return a random landing site for a seed dispersed by this food source.
	 * The landing sites are normally distributed around this location.
	 * @return The location of the landing site.
	 */
	public Environment.Location getRandomSeedLandingSite() {
		int randX, randY;
		double sx, sy;
		Environment.Location randLoc;

		do {
			//get the standard deviations for x and y
			sy = this.getStdDev(Environment.AXIS_Y);
			sx = this.getStdDev(Environment.AXIS_X);

			//get random coordinates for x and y based on transformation of standard normal
			randX = (int) Math.floor(cobweb.globals.random.nextGaussian() * sx + this.position.v[0]);
			randY = (int) Math.floor(cobweb.globals.random.nextGaussian() * sy + this.position.v[1]);

			randLoc = this.environment.getLocation(randX, randY);
			//make sure that the coordinates are valid (99.7%) AND that they are not the same as these
		} while (randLoc == null);

		return randLoc;
	}

	/**
	 * Helper function for seed scattering.
	 * Return the standard deviation computed for the given axis from this point.
	 * @param axis The axis along which to compute the standard deviation.
	 * @return The standard deviation being used.
	 */
	private double getStdDev(int axis) {
		//most of normal distribution is contained within 3 standard deviations from mean (99.7%)

		//		int distTop = this.environment.getSize(axis) / 6 - this.coords.v[axis];
		//		int distBottom = this.coords.v[axis];
		//
		//		return Math.min(distTop, distBottom) / 3;

		return this.environment.getSize(axis) / 6.0;
	}

	/**
	 * Deplete this food source once.
	 * TODO someone, please call this function
	 */
	public void deplete() {
		//now the food left changes according to the age of the food source
		this.foodLeft = (int) Math.round(-1 * this.maxFood * Math.pow((this.age - this.maxAge / 2), 2));
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
	 * Remove a piece of food from the food source.
	 * @return A piece of food that is of the same type as the food source. Null, if no food.
	 */
	public final Food getFood() {
		this.foodLeft--;

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

	/**
	 * Only an agent or waste can go on top of a food source.
	 * @param other Another cell object.
	 * @return True if other is an agent or food, false otherwise.
	 */
	@Override
	public boolean canCoverWith(CellObject other) {
		return other instanceof Agent || other instanceof Waste;
	}

	/**
	 * Food Sources cannot move.
	 */
	@Override
	public boolean canMove() {
		return false;
	}
}
