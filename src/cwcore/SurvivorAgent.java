package cwcore;

import java.util.ArrayList;

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

	/**
	 * TODO set this in XML
	 * The maximum carrying capacity for this agent.
	 */
	private final int MAX_CARRY_CAPACITY = 2;

	/**
	 * A collection of carried food.
	 */
	private ArrayList<Food> carriedFood;

	/**
	 * TODO set this in XML
	 * If set to true, eat the food with the most benefit.
	 * If set to false, eat the food at random.
	 */
	private static final boolean EAT_SMART = true;

	/**
	 * TODO set this in XML
	 * Refer to this variable when a piece of food is being added to the full inventory.
	 * If set to false, do not add the food.
	 * If set to true, compare this food with the inventory food of the lowest benefit.
	 * If this food has larger benefit, then drop the food with the lowest benefit and add this food.
	 */
	private final boolean CARRY_SMART = false;

	/**
	 * Create a new CommAgent.
	 */
	public SurvivorAgent () {
		//carried food is an array list
		this.carriedFood = new ArrayList<Food>();
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
		int worstIndex = this.getWorstFoodIndex();
		int worstBenefit = this.getFoodBenefit(this.carriedFood.get(worstIndex));

		int thisBenefit = this.getFoodBenefit(food);

		if(thisBenefit > worstBenefit) {
			this.dropFood(worstIndex);
			this.carriedFood.add(food);
			return true;
		} else {
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
		if(this.carriedFood.size() == this.MAX_CARRY_CAPACITY) {
			return false;
		} else {
			this.carriedFood.add(food);
			return true;
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
	 * @return Food benefit of the given food.
	 */
	public int getFoodBenefit(Food f) {
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
			int bestBenefit = -1;
			int benefit;

			for(int i = 0; i < this.carriedFood.size(); i++) {
				benefit = this.getFoodBenefit(this.carriedFood.get(i));

				if(benefit > bestBenefit) {
					bestIndex = i;
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
			int worstBenefit = -1;
			int benefit;

			for(int i = 0; i < this.carriedFood.size(); i++) {
				benefit = this.getFoodBenefit(this.carriedFood.get(i));

				if(benefit < worstBenefit) {
					worstIndex = i;
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
	 * Remove that food from the inventory.
	 */
	private void smartEatCarriedFood() {
		int eatIndex = this.getBestFoodIndex();
		Food f = this.carriedFood.remove(eatIndex);
		//TODO note that this won't work right now since this method doesn't exist
		//		this.eat(f);
	}

	/**
	 * Eat a random piece of food from the inventory.
	 * Remove that food from the inventory.
	 */
	private void eatRandomCarriedFood() {
		int eatIndex = (int) Math.floor(Math.random() * this.carriedFood.size());
		Food f = this.carriedFood.remove(eatIndex);
		//TODO note that this won't work right now since this method doesn't exist
		//		this.eat(f);
	}
}
