package cwcore;

import java.util.ArrayList;
import java.util.LinkedList;

import cobweb.Environment;

/**
 * Added functionality over ComplexAgent:
 * 
 * <ol>
 * <li>Simple path finding</li>
 * <li>Non-random movement - directed towards some goal</li>
 * <li>Ability to carry food/ consume carried food</li>
 * <li>Instinct of self-preservation - knows when hungry, tries to find food</li>
 * <li>Memory of resource locations</li>
 * </ol>
 * 
 * @author Daniel Kats
 */
public class SurvivorAgent extends ComplexAgent {
	/**
	 * Auto-generated ID for serialization.
	 */
	private static final long serialVersionUID = -3614238409591026125L;

	/*************************************************************************************
	 * ****************************** STATIC VARIABLES ***********************************
	 *************************************************************************************/

	/**
	 * TODO set this in XML
	 * The maximum number of food sources this agent can remember at a time.
	 */
	private final int MAX_FOOD_SOURCE_MEMORY = 1;

	/**
	 * TODO set this in XML
	 * The maximum carrying capacity for this agent.
	 */
	private final int MAX_CARRY_CAPACITY = 2;

	/**
	 * TODO set this in XML
	 * TODO maybe add this to DNA
	 * Refer to this variable when food is being eaten.
	 * If set to true, eat the food with the most benefit.
	 * If set to false, eat the food at random.
	 */
	private final boolean EAT_SMART = true;

	/**
	 * TODO set this in XML
	 * TODO maybe add this to DNA
	 * Refer to this variable when food is being added to a full inventory.
	 * If set to false, do not add the food.
	 * If set to true, compare this food with the inventory food of the lowest benefit.
	 * If this food has larger benefit, then drop the food with the lowest benefit and add this food.
	 */
	private final boolean CARRY_SMART = false;

	/**
	 * TODO set this in XML
	 * TODO maybe add this to DNA
	 * Refer to this variable when remembering food locations.
	 * If set to true, remember the location of the highest-yield food.
	 * If set to false, remember the most recently-added location.
	 * TODO in the future, viable options are:
	 * 1. REMEMBER_CLOSEST (closest)
	 * 2. REMEMBER_NEWEST (most recently memorized)
	 * 3. REMEMBER_HIGH_YIELD (location with highest-yield food)
	 * 4. REMEMBER_SMART (weighting of yield, competition for food, and distance)
	 */
	private final boolean REMEMBER_SMART = false;

	/*************************************************************************************
	 * ****************************** INSTANCE VARIABLES *********************************
	 *************************************************************************************/

	/**
	 * Inventory of carried food.
	 */
	private ArrayList<Food> carriedFood;

	/**
	 * Memory of locations of food sources.
	 */
	private LinkedList<Environment.Location> foodSources;

	/*************************************************************************************
	 * ****************************** CONSTRUCTOR ****************************************
	 *************************************************************************************/

	/**
	 * Create a new Survivor Agent.
	 */
	public SurvivorAgent () {
		//carried food is an array list
		this.carriedFood = new ArrayList<Food>();
		this.foodSources = new LinkedList<Environment.Location>();
	}

	/**
	 * This method 
	 */
	@Override
	public void control() {

	}

	/*************************************************************************************
	 * ****************************** FUNCTIONS ******************************************
	 *************************************************************************************/


	/**
	 * Memorise the location of the given food source.
	 * If agent's memory is full, agent forgets oldest food source.
	 * @param foodSourceLocation Location of a food source.
	 */
	public void rememberFoodSource(Environment.Location foodSourceLocation) {
		foodSources.add(foodSourceLocation);

		while(foodSources.size() > this.MAX_FOOD_SOURCE_MEMORY) {
			this.foodSources.remove();
		}
	}

	/**
	 * Return the location of a food source.
	 * If memories are empty, return null.
	 * TODO for now return the oldest one.
	 * @return The location of a food source.
	 */
	public Environment.Location rememberFoodSourceLocation() {
		if(this.foodSources.isEmpty()) {
			return null;
		} else {
			if(this.REMEMBER_SMART) {
				return this.highYieldRemember();
			} else {
				return this.stupidRemember();
			}
		}
	}

	/**
	 * Return the location of the food source with the highest-yield food.
	 * If memories are empty, return null.
	 * TODO NOT IMPLEMENTED YET
	 * @return The location of a food source.
	 */
	private Environment.Location highYieldRemember() {
		//TODO for now
		return this.stupidRemember();
	}

	/**
	 * Return the location of the newest remembered food source.
	 * If memories are empty, return null.
	 * @return The location of a food source.
	 */
	private Environment.Location stupidRemember() {
		if(this.foodSources.isEmpty()) {
			return null;
		} else {
			return this.foodSources.getLast();
		}
	}

	/**
	 * Add food to inventory if inventory is not full.
	 * If inventory is full, add the food if it has greater food than some other existing food.
	 * Drop the food of least benefit.
	 * Return true if food was added, false otherwise.
	 * @param food The food to add.
	 * @return True if food was added, false otherwise.
	 */
	private boolean smartCarryFood(Food food) {
		if(this.carriedFood.size() < this.MAX_CARRY_CAPACITY) {
			this.carriedFood.add(food);
			return true;
		} else if (this.MAX_CARRY_CAPACITY > 0) {
			int worstIndex = this.getWorstFoodIndex();
			//shouldn't return -1 because carriedFood is full
			int worstBenefit = this.getFoodBenefit(this.carriedFood.get(worstIndex));
			int thisBenefit = this.getFoodBenefit(food);

			if(thisBenefit > worstBenefit) {
				this.dropFood(worstIndex);
				this.carriedFood.add(food);
				return true;
			} else {
				return false;
			}	
		} else {
			//go here if MAX_CARRY_CAPACITY set to 0
			return false;
		}
	}

	/**
	 * Drop the food at the given index in the inventory.
	 * Return the food that was dropped.
	 * If the index is invalid, return null.
	 * TODO for now just removes from inventory
	 * @param foodIndex Index in the food inventory.
	 * @return The food that was dropped.
	 */
	public Food dropFood(int foodIndex) {
		if(foodIndex >= 0 && foodIndex < this.carriedFood.size()) {
			return this.carriedFood.remove(foodIndex);
		} else {
			return null;
		}
	}

	/**
	 * Add food to inventory if inventory is not full.
	 * Do not add food to inventory if inventory is full.
	 * Return true if food was added, false otherwise.
	 * @param food The food to add.
	 * @return True if food was added, false otherwise.
	 */
	private boolean stupidCarryFood(Food food) {
		if(this.carriedFood.size() < this.MAX_CARRY_CAPACITY) {
			this.carriedFood.add(food);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Carry the given food.
	 * Return true if the food is added to inventory, false otherwise.
	 * @param food The food to carry.
	 * @return True if the food was equipped (added to inventory), false otherwise.
	 */
	public boolean carryFood(Food food) {
		if(this.CARRY_SMART) {
			return this.smartCarryFood(food);
		} else {
			return this.stupidCarryFood(food);
		}
	}

	/**
	 * Return the food benefit of the given food to this agent.
	 * If food is null, return 0.
	 * @return Food benefit of the given food.
	 */
	public int getFoodBenefit(Food f) {
		if(f == null) {
			return 0;
		}

		int foodType = f.getType();

		if(foodType == this.type()) {
			return params.foodEnergy;
		} else {
			return params.otherFoodEnergy;
		}
	}

	/**
	 * Return the index in food where the food has highest benefit.
	 * If inventory is empty, return -1.
	 * TODO This is basically a call to MAX. Maybe implement PQ?
	 * @return The index in food where the food has highest benefit.
	 */
	private int getBestFoodIndex() {
		if(this.carriedFood.isEmpty()) {
			return -1;
		} else {
			int bestIndex = 0;
			int bestBenefit = -1, benefit;

			for(int i = 0; i < this.carriedFood.size(); i++) {
				benefit = this.getFoodBenefit(this.carriedFood.get(i));

				//i == 0 check or fails for sets where highest benefit < -1
				if(i == 0 || benefit > bestBenefit) {
					bestIndex = i;
					bestBenefit = benefit;
				}
			}

			return bestIndex;
		}
	}

	/**
	 * Return the index in food where the food has lowest benefit.
	 * If inventory is empty, return -1.
	 * TODO This is basically a call to MIN. Maybe implement PQ?
	 * @return The index in food where the food has lowest benefit.
	 */
	private int getWorstFoodIndex() {
		if(this.carriedFood.isEmpty()) {
			return -1;
		} else {
			int worstIndex = 0;
			int worstBenefit = -1, benefit;

			for(int i = 0; i < this.carriedFood.size(); i++) {
				benefit = this.getFoodBenefit(this.carriedFood.get(i));

				//i == 0 check otherwise always returns -1
				if(i == 0 || benefit < worstBenefit) {
					worstIndex = i;
					worstBenefit = benefit;
				}
			}

			return worstIndex;
		}
	}

	/**
	 * Eat a piece of carried food, if there is food.
	 * Return whether food was eaten.
	 * @return True if food was eaten, false otherwise.
	 */
	public boolean eatCarriedFood() {
		if(!this.carriedFood.isEmpty()){
			if(EAT_SMART) {
				this.smartEatCarriedFood();
			} else {
				this.eatRandomCarriedFood();
			}

			return true;
		}

		return false;
	}

	/**
	 * Eat the carried food with the greatest benefit.
	 * Don't eat anything if inventory is empty.
	 * Remove that food from the inventory.
	 */
	private void smartEatCarriedFood() {
		if(!this.carriedFood.isEmpty()) {
			int eatIndex = this.getBestFoodIndex();
			Food f = this.carriedFood.remove(eatIndex);
			//TODO note that this won't work right now since this method doesn't exist
			//		this.eat(f);
		}
	}

	/**
	 * Eat a random piece of food from the inventory.
	 * Don't eat anything if inventory is empty.
	 * Remove that food from the inventory.
	 */
	private void eatRandomCarriedFood() {
		if(!this.carriedFood.isEmpty()) {
			int eatIndex = (int) Math.floor(Math.random() * this.carriedFood.size());
			Food f = this.carriedFood.remove(eatIndex);
			//TODO note that this won't work right now since this method doesn't exist
			//		this.eat(f);
		}
	}
}
