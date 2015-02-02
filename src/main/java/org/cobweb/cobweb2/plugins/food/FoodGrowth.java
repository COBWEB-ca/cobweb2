package org.cobweb.cobweb2.plugins.food;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cobweb.cobweb2.core.Direction;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.cobweb2.plugins.EnvironmentMutator;
import org.cobweb.cobweb2.plugins.abiotic.AbioticMutator;
import org.cobweb.cobweb2.plugins.abiotic.AbioticPreferenceParam;


public class FoodGrowth implements EnvironmentMutator {

	private static final int DROP_ATTEMPTS_MAX = 5;

	private ComplexFoodParams foodData[];

	private int draughtdays[];

	private SimulationTimeSpace simulation;

	private ComplexEnvironment env;

	private float sameFoodProb;

	private boolean dropNew;

	public FoodGrowth(SimulationTimeSpace simulation) {
		this.simulation = simulation;
	}

	private void depleteFood() {
		// for each agent type, we test to see if its deplete time step has
		// come, and if so deplete the food random
		// by the appropriate percentage

		for (int type = 0; type < foodData.length; type++) {
			ComplexFoodParams f = foodData[type];

			if (f.depleteRate != 0.0f
					&& f.growRate > 0
					&& simulation.getTime() != 0
					&& (simulation.getTime() % f.depleteTime) == 0) {
				depleteFood(f, type);
			}
		}
	}

	private void depleteFood(ComplexFoodParams food, int type) {
		// the algorithm for randomly selecting the food cells to delete
		// is as follows:
		// We iterate through all of the cells and the location of each
		// one containing food type i is added to
		// a random position in our vector. We then calculate exactly
		// how many food items we need to destroy, say N,
		// and we destroy the food at the positions occupying the last N
		// spots in our vector
		List<Location> locations = new ArrayList<Location>();
		for (int x = 0; x < simulation.getTopology().width; ++x) {
			for (int y = 0; y < simulation.getTopology().height; ++y) {
				Location currentPos = new Location(x, y);
				if (env.hasFood(currentPos) && env.getFoodType(currentPos) == type)
					locations.add(currentPos);
			}
		}

		Collections.shuffle(locations, simulation.getRandom());

		int foodToDeplete = (int) (locations.size() * food.depleteRate);

		for (int j = 0; j < foodToDeplete; ++j) {
			Location loc = locations.get(j);

			env.removeFood(loc);
		}
		draughtdays[type] = food.draughtPeriod;
	}

	private void dropFood(int type) {
		float foodDrop = foodData[type].dropRate;
		while (simulation.getRandom().nextFloat() < foodDrop) {
			--foodDrop;
			Location l;
			int j = 0;
			do {
				++j;
				l = simulation.getTopology().getRandomLocation();

			} while (j < DROP_ATTEMPTS_MAX &&  env.hasAnythingAt(l));

			if (j < DROP_ATTEMPTS_MAX) {
				env.addFood(l, type);
			}
		}
	}

	private void growFood() {
		// create a new ArrayEnvironment and a new food type array
		// loop through all positions

		AbioticMutator abiotic = env.getPlugin(AbioticMutator.class);

		for (int y = 0; y < simulation.getTopology().height; ++y) {
			for (int x = 0; x < simulation.getTopology().width; ++x) {
				Location currentPos = new Location(x, y);

				if (!env.hasAnythingAt(currentPos)) {
					// we should grow food here
					// the following code block tests all adjacent squares
					// to this one and counts how many have food
					// as well how many of each food type exist

					double foodCount = 0;
					int[] mostFood = new int[getTypeCount()];

					for (Direction dir : simulation.getTopology().ALL_4_WAY) {
						Location checkPos = simulation.getTopology().getAdjacent(currentPos, dir);
						if (checkPos != null && env.hasFood(checkPos)) {
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

						if (sameFoodProb >= simulation.getRandom().nextFloat()) {
							growingType = max;
						} else {
							growingType = simulation.getRandom().nextInt(getTypeCount());
						}

						// finally, we grow food according to a certain
						// amount of random chance
						ComplexFoodParams thisType = foodData[growingType];
						float growRate = thisType.growRate;

						for (int i = 0; i < thisType.abioticParams.factorParams.length; i++) {
							AbioticPreferenceParam factorParams = thisType.abioticParams.factorParams[i].preference;
							float factorValue = abiotic.getValue(i, currentPos);
							float discomfort = 1 + factorParams.score(factorValue);
							growRate *= discomfort;
						}

						if (foodCount * growRate > 100 * simulation.getRandom().nextFloat()) {
							env.addFood(currentPos, growingType);
						}
					}
				}
			}
		}
	}

	/**
	 * Initializes drought days for each food type to zero.  Also checks to see if
	 * food deplete rates and times are valid for each food type.  Valid random food
	 * deplete rates and times will be generated using the environments random number
	 * generator for each invalid entry.
	 */
	private void setupDraughtDeplete() {
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
	public void loadNewFood() {
		for (int i = 0; i < getTypeCount(); ++i) {
			for (int j = 0; j < foodData[i].initial; ++j) {
				Location l;
				int tries = 0;
				do {
					l = simulation.getTopology().getRandomLocation();
				} while (tries++ < 100 && env.hasAnythingAt(l));
				if (tries < 100) {
					env.addFood(l, i);
				}
			}
		}
	}

	private int getTypeCount() {
		return foodData.length;
	}

	@Override
	public void update() {
		depleteFood();

		boolean shouldGrow = false;
		for (ComplexFoodParams f : foodData) {
			if (f.growRate > 0) {
				shouldGrow = true;
				break;
			}
		}

		// if no food is growing (total == 0) this loop is not necessary
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

	public void setParams(ComplexEnvironment environment, FoodGrowthParams foodParams) {
		foodData = foodParams.foodParams;
		this.sameFoodProb = foodParams.likeFoodProb;
		dropNew = foodParams.dropNewFood;

		env = environment;
	}

	@Override
	public void loadNew() {
		setupDraughtDeplete();

		// add food to random locations
		if (dropNew) {
			loadNewFood();
		}
	}

}
