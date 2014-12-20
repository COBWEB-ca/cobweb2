package org.cobweb.cobweb2.food;

import java.util.LinkedList;

import org.cobweb.cobweb2.core.ArrayEnvironment;
import org.cobweb.cobweb2.core.Direction;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.params.ComplexFoodParams;


public class Food {

	private static final int DROP_ATTEMPTS_MAX = 5;

	private ComplexFoodParams foodData[];

	private int draughtdays[];

	ArrayEnvironment backArray;
	int[][] backFoodArray;

	private SimulationInternals simulation;

	private Environment env;

	private float likeFoodProb;

	public Food(SimulationInternals simulation) {
		this.simulation = simulation;
	}

	private void depleteFood(ComplexFoodParams food) {
		// the algorithm for randomly selecting the food cells to delete
		// is as follows:
		// We iterate through all of the cells and the location of each
		// one containing food type i is added to
		// a random position in our vector. We then calculate exactly
		// how many food items we need to destroy, say N,
		// and we destroy the food at the positions occupying the last N
		// spots in our vector
		LinkedList<Location> locations = new LinkedList<Location>();
		for (int x = 0; x < env.topology.width; ++x)
			for (int y = 0; y < env.topology.height; ++y) {
				Location currentPos = new Location(x, y);
				if (env.testFlag(currentPos, Environment.FLAG_FOOD) && env.getFoodType(currentPos) == food.type)
					locations.add(simulation.getRandom().nextInt(locations.size() + 1), currentPos);
			}

		int foodToDeplete = (int) (locations.size() * food.depleteRate);

		for (int j = 0; j < foodToDeplete; ++j) {
			Location loc = locations.removeLast();

			env.setFlag(loc, Environment.FLAG_FOOD, false);
		}
		draughtdays[food.type] = food.draughtPeriod;
	}

	private void dropFood(int type) {
		float foodDrop = foodData[type].dropRate;
		while (simulation.getRandom().nextFloat() < foodDrop) {
			--foodDrop;
			Location l;
			int j = 0;
			do {
				++j;
				l = env.topology.getRandomLocation();

			} while (j < DROP_ATTEMPTS_MAX
					&& (env.testFlag(l, Environment.FLAG_STONE) || env.testFlag(l, Environment.FLAG_FOOD)
							|| env.testFlag(l, Environment.FLAG_DROP) || env.getAgent(l) != null));

			if (j < DROP_ATTEMPTS_MAX) {
				env.addFood(l, type);
			}
		}
	}

	private void growFood() {

		for (int y = 0; y < env.topology.height; ++y) {
			for (int x = 0; x < env.topology.width; ++x) {
				Location currentPos = new Location(x, y);
				// if there's a stone or already food, we simply copy the
				// information from the old arrays to the new ones
				backArray.setLocationBits(currentPos, env.array.getLocationBits(currentPos));
				backFoodArray[currentPos.x][currentPos.y] = env.foodarray[currentPos.x][currentPos.y];
			}
		}

		// create a new ArrayEnvironment and a new food type array
		// loop through all positions
		for (int y = 0; y < env.topology.height; ++y) {
			for (int x = 0; x < env.topology.width; ++x) {
				Location currentPos = new Location(x, y);
				// if there's a stone or already food, we simply copy the
				// information from the old arrays to the new ones
				if ((env.array.getLocationBits(currentPos) & Environment.MASK_TYPE) == 0) {
					// otherwise, we want to see if we should grow food here
					// the following code block tests all adjacent squares
					// to this one and counts how many have food
					// as well how many of each food type exist

					double foodCount = 0;
					int[] mostFood = new int[getTypeCount()];

					for (Direction dir : env.topology.ALL_4_WAY) {
						Location checkPos = env.topology.getAdjacent(currentPos, dir);
						if (checkPos != null && env.testFlag(checkPos, Environment.FLAG_FOOD)) {
							foodCount++;
							mostFood[env.getFoodType(checkPos)]++;
						}
					}

					// and if we have found any adjacent food, theres a
					// chance we want to grow food here
					if (foodCount > 0) {

						int max = 0;
						int growingType;

						// find the food that exists in the largest quantity
						for (int i = 1; i < mostFood.length; ++i)
							if (mostFood[i] > mostFood[max])
								max = i;

						// give the max food an extra chance to be chosen

						if (likeFoodProb >= simulation.getRandom().nextFloat()) {
							growingType = max;
						} else {
							growingType = simulation.getRandom().nextInt(getTypeCount());
						}

						// finally, we grow food according to a certain
						// amount of random chance
						if (foodCount * foodData[growingType].growRate > 100 * simulation.getRandom().nextFloat()) {
							backArray.setLocationBits(currentPos, Environment.FOOD_CODE);
							// setFoodType (currentPos, growMe);
							backFoodArray[currentPos.x][currentPos.y] = growingType;
						} else {
							backArray.setLocationBits(currentPos, 0);
							backFoodArray[currentPos.x][currentPos.y] = -123154534;
						}
					} else {
						backArray.setLocationBits(currentPos, 0);
						backFoodArray[currentPos.x][currentPos.y] = -123154534;
					}
				}
			}
		}

		// The tile array we've just computed becomes the current tile array
		ArrayEnvironment swapArray = env.array;
		env.array = backArray;
		backArray = swapArray;

		int[][] swapFoodArray = env.foodarray;
		env.foodarray = backFoodArray;
		backFoodArray = swapFoodArray;

	}

	/**
	 * Initializes drought days for each food type to zero.  Also checks to see if
	 * food deplete rates and times are valid for each food type.  Valid random food
	 * deplete rates and times will be generated using the environments random number
	 * generator for each invalid entry.
	 */
	private void loadFoodMode() {
		draughtdays = new int[getTypeCount()];
		for (int i = 0; i < getTypeCount(); ++i) {
			draughtdays[i] = 0;
			if (foodData[i].depleteRate < 0.0f || foodData[i].depleteRate > 1.0f)
				foodData[i].depleteRate = simulation.getRandom().nextFloat();
			if (foodData[i].depleteTime <= 0)
				foodData[i].depleteTime = simulation.getRandom().nextInt(100) + 1;
		}
	}

	/**
	 * Randomly places food in the environment.
	 */
	private void loadNewFood() {
		for (int i = 0; i < getTypeCount(); ++i) {
			for (int j = 0; j < foodData[i].initial; ++j) {
				Location l;
				int tries = 0;
				do {
					l = env.topology.getRandomLocation();
				} while ((tries++ < 100)
						&& (env.testFlag(l, Environment.FLAG_STONE) || env.testFlag(l, Environment.FLAG_DROP)));
				if (tries < 100) {
					env.addFood(l, i);
				}
			}
		}
	}

	private int getTypeCount() {
		return foodData.length;
	}

	public void update(long tick) {


		// for each agent type, we test to see if its deplete time step has
		// come, and if so deplete the food random
		// by the appropriate percentage

		for (ComplexFoodParams f : foodData) {
			if (f.depleteRate != 0.0f
					&& f.growRate > 0
					&& (simulation.getTime() % f.depleteTime) == 0) {
				depleteFood(f);
			}
		}

		boolean shouldGrow = false;
		for (ComplexFoodParams f : foodData) {
			if (f.growRate > 0) {
				shouldGrow = true;
				break;
			}
		}

		// if no food is growing (total == 0) this loop is not nessesary
		if (shouldGrow) {
			growFood();
		}

		// Air-drop food into the environment
		for (int i = 0; i < getTypeCount(); ++i) {
			if (draughtdays[i] == 0) {
				dropFood(i);
			} else {
				draughtdays[i]--;
			}
		}
	}

	public void load(boolean dropNewFood, float likeFoodProb, ComplexFoodParams[] foodParams) {
		foodData = foodParams;

		env = simulation.getEnvironment();

		backFoodArray = new int[env.topology.width][env.topology.height];
		backArray = new ArrayEnvironment(env.topology.width, env.topology.height);

		this.likeFoodProb = likeFoodProb;

		loadFoodMode();

		// add food to random locations
		if (dropNewFood) {
			loadNewFood();
		}
	}

}
