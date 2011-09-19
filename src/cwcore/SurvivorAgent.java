package cwcore;

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
	private static final int MAX_CARRY_CAPACITY = 2;

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
	 * Create a new CommAgent.
	 */
	public SurvivorAgent () {

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
	private int getBestFoodIndex() {
		int bestIndex = 0;
		boolean better = true;

		for(int i = 1; i < MAX_CARRY_CAPACITY; i++) {
			//get the benefit of the food at index i and store it in 'better'
			if(better) {
				bestIndex = i;
			}
		}

		return bestIndex;
	}

	/**
	 * Eat the carried food with the greatest benefit.
	 */
	private void eatSmartCarriedFood() {
		int eatIndex = getBestFoodIndex();

		Food f;

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
