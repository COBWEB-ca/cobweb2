package cwcore;

import java.util.ArrayList;
import java.util.Collection;

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
	private Collection<Food> carriedFood;

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
		//TODO add functionality
		//For now just does stupid carry
		return this.stupidCarryFood(food);
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
			//return the comp.
			//TODO for now
			return 1;
		} else {
			//return the non-comp.
			//TODO for now
			return 0;
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
				this.eatSmartCarriedFood();
			} else {
				this.eatRandomCarriedFood();
			}

			return true;
		}

		return false;
	}

	/**
	 * Return the index in food where the food has highest benefit.
	 * @return The index in food where the food has highest benefit.
	 */
	private Food getBestFood() {
		if(this.carriedFood.isEmpty()) {
			return null;
		} else {
			//TODO for now does this, not fully implemented
			return ((ArrayList<Food>) this.carriedFood).get(0);
		}
	}

	/**
	 * Eat the carried food with the greatest benefit.
	 */
	private void eatSmartCarriedFood() {
		//TODO not implemented yet
		//		Food f = getBestFood();

		//get the food at index eatIndex and put it into Food f
		//remove the food at eatIndex
		//eat food F
	}

	/**
	 * Eat a random piece of food in the carried collection.
	 */
	private void eatRandomCarriedFood() {
		//get a number between 0 and the size(food)
		//remove that food
		//eat that food
	}
}
