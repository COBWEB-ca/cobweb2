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
	 * The maximum distance the agent can see.
	 */
	private final int MAX_SEE_SQUARE_DIST = 16;

	/**
	 * The maximum angle the agent can see from the direction it is facing.
	 */
	private final double MAX_SEE_ANGLE = Math.PI / 4;

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
	 * <ol>
	 * <li> REMEMBER_CLOSEST (closest) </li>
	 * <li> REMEMBER_NEWEST (most recently memorized) </li>
	 * <li> REMEMBER_HIGH_YIELD (location with highest-yield food) </li>
	 * <li> REMEMBER_SMART (weighting of yield, competition for food, and distance) </li>
	 * </ol>
	 */
	private final boolean REMEMBER_SMART = false;

	/**
	 * If the agent has less than this amount of energy, it's hungry.
	 * TODO allow user to configure this.
	 * TODO pass this on in DNA
	 */
	private final int HUNGER_THRESHOLD = GeneticController.ENERGY_THRESHOLD / 2;

	/*************************************************************************************
	 * ************************************ ENUM *****************************************
	 *************************************************************************************/

	/**
	 * The possible states for this agent.
	 */
	protected enum State {
		/*
		 * Looking for a food source.
		 */
		HUNGRY,
		/*
		 * Exploring area.
		 */
		EXPLORING
	}

	/**
	 * Possible primitive actions for this agent.
	 */
	protected enum Action {
		TURN_LEFT,
		TURN_RIGHT,
		MOVE_FORWARD
	}

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
	private LinkedList<FoodSource> foodSources;

	/**
	 * This agent's state.
	 */
	private State state;

	/*************************************************************************************
	 * ****************************** CONSTRUCTOR ****************************************
	 *************************************************************************************/

	/**
	 * Create a new Survivor Agent.
	 */
	public SurvivorAgent () {
		//carried food is an array list
		this.carriedFood = new ArrayList<Food>();
		this.foodSources = new LinkedList<FoodSource>();
	}

	/**
	 * Method to call at beginning of turn.
	 */
	@Override
	protected void control() {
		//figures out the state
		state = isHungry() ? State.HUNGRY : State.EXPLORING;

		//acts on the state
		switch(this.state) {
			case HUNGRY:
				this.findFood();
				break;
			case EXPLORING:
				this.explore();
				break;
		}

		//end turn here
	}

	/**
	 * Find food. Eat it.
	 */
	protected void findFood() {
		//this is linear because can only eat once per tick

		if(this.carriedFood.isEmpty()) {
			SeeInfo si = this.distanceLook();

			if(si.getType() == ComplexEnvironment.FLAG_FOOD) {
				int dist = si.getDist();

				//get the location of the food
				Environment.Location foodLocation = this.position.add(dist, this.facing);

				//get the food source object at that location
				//TODO for now
				FoodSource source = null;

				//memorize the food source
				this.rememberFoodSource(source);

				//if it's adjacent
				if(dist == 1) {
					//eat the food
					Food f = source.getFood();
					//TODO add eat method here
				} else {
					//move to the location
					this.moveToLocation(foodLocation);
				}
			} else {
				if(this.foodSources.isEmpty()) {
					//move randomly
					this.explore();
				} else {
					//find a food source location
					Environment.Location foodLocation = this.rememberFoodSourceLocation().getLocation();

					//move to the food source
					this.moveToLocation(foodLocation);
				}
			}
		} else {
			//eat the carried food
			this.eatCarriedFood();
		}
	}

	/**
	 * Wander aimlessly. Memorise food sources.
	 */
	protected void explore() {
		boolean seeFoodSource;
		Action nextAction;


		//Look for unfamiliar food source
		//TODO for now
		seeFoodSource = false;

		if(seeFoodSource) {
			//get direction to food source
			//TODO for now
			nextAction = Action.MOVE_FORWARD;
		} else {
			nextAction = this.getRandomAction();
		}		

		this.doMove(nextAction);
	}

	/*************************************************************************************
	 * ****************************** FUNCTIONS ******************************************
	 *************************************************************************************/

	/**
	 * Return true if the agent can see the given tile, false otherwise.
	 * @param tile The tile being examined.
	 * @return True if the agent can see the tile, false otherwise.
	 */
	private final boolean canSee(final Environment.Location tile) {
		double facingAngle = this.facing.angle();
		double targetAngle = this.position.angleTo(tile);

		return Math.abs(targetAngle - facingAngle) <= this.MAX_SEE_ANGLE &&
		this.position.distanceSquare(tile) <= this.MAX_SEE_SQUARE_DIST;
	} 

	/**
	 * Move to the given location.
	 * @param coords Coordinates of the location.
	 */
	protected void moveToLocation(Environment.Location coords) {
		//TODO path-finding is not fully implemented

		//simple path-finding algos here

		//TODO maybe have different path-finding algos, which take different 

		//very human algo.
		//go to the place which is in the "general" direction of the destination
		//if none exists, go to a random spot

		//gen. dir. = the resultant square will make the distance >=
	}

	/**
	 * Return true if this agent is hungry, false otherwise.
	 * @return True if the agent is hungry, false otherwise.
	 */
	protected boolean isHungry() {
		return this.energy < this.HUNGER_THRESHOLD;
	}

	/**
	 * Perform the given move.
	 * @param action The action to perform.
	 */
	protected void doMove(Action action) {
		switch(action) {
			case TURN_LEFT:
				super.turnLeft();
				break;
			case TURN_RIGHT:
				super.turnRight();
				break;
			case MOVE_FORWARD:
				super.step();
				break;
		}
	}

	/**
	 * Return a random action.
	 * @return Random action.
	 */
	private Action getRandomAction() {
		int numActions = Action.values().length;
		int actionIndex = (int) Math.floor(Math.random() * numActions);
		return Action.values()[actionIndex];
	}

	/**
	 * Memorise the location of the given food source, if it is new.
	 * If agent's memory is full, agent forgets oldest food source.
	 * @param foodSource A food source.
	 * @return True if the food source was remembered, was otherwise.
	 */
	protected boolean rememberFoodSource(FoodSource foodSource) {
		if(foodSources.contains(foodSource)) {
			return false;
		} else {
			foodSources.add(foodSource);

			while(foodSources.size() > this.MAX_FOOD_SOURCE_MEMORY) {
				this.foodSources.remove();
			}

			return true;
		}
	}

	/**
	 * Return the location of a food source.
	 * If memories are empty, return null.
	 * @return The location of a food source.
	 */
	protected FoodSource rememberFoodSourceLocation() {
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
	 * @return The location of a food source.
	 */
	private FoodSource highYieldRemember() {
		if(this.foodSources.isEmpty()) {
			return null;
		} else {
			int bestIndex = this.getBestFoodSourceIndex();
			return this.foodSources.get(bestIndex);
		}
	}

	/**
	 * Return the location of the newest remembered food source.
	 * If memories are empty, return null.
	 * @return The location of a food source.
	 */
	private FoodSource stupidRemember() {
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
	protected Food dropFood(int foodIndex) {
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
	protected boolean carryFood(Food food) {
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
	protected int getFoodBenefit(Food f) {
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
	 * Return the food benefit of the given food type to this agent.
	 * @return Food benefit of the given food.
	 */
	protected int getFoodBenefit(int type) {
		return this.getFoodBenefit(new Food(type));
	}

	/**
	 * Return the index in memory where the food has highest benefit.
	 * If memory is empty, return -1.
	 * TODO This is basically a call to MAX. Maybe implement PQ?
	 * @return The index in memory where the food has highest benefit.
	 */
	private int getBestFoodSourceIndex() {
		if(this.foodSources.isEmpty()) {
			return -1;
		} else {
			int bestIndex = 0;
			int bestBenefit = -1, benefit;

			for(int i = 0; i < this.foodSources.size(); i++) {
				benefit = this.getFoodBenefit(this.foodSources.get(i).getType());

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

				//check i == 0 otherwise fails for sets where highest benefit < -1
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
	protected boolean eatCarriedFood() {
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
