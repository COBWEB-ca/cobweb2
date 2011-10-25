package cell;


import java.util.Iterator;

import cobweb.Agent;
import cobweb.DrawingHandler;
import cwcore.Food;

/**
 * A cell.
 */
public class Cell extends Agent {

	/**
	 * Cells cannot move.
	 */
	@Override
	public boolean canMove() {
		return false;
	}

	/**
	 * Cells cannot swim (yet)
	 */
	@Override
	public boolean canSwim() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Get nutrients from blood.
	 * @param b Given blood.
	 */
	public void getNutrients(Blood b) {
		//extract all food from the blood
		Iterator<Food> food = b.getFood();

		//and eat all the food
		while(food.hasNext()) {
			//TODO do not eat what is not needed.
			this.eat(food.next());
		}
	}

	@Override
	public void getDrawInfo(DrawingHandler theUI) {
		// TODO Auto-generated method stub

	}

	@Override
	public double similarity(Agent other) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double similarity(int other) {
		// TODO Auto-generated method stub
		return 0;
	}
}
