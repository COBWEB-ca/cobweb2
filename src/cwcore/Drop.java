package cwcore;

import java.awt.Color;

import cobweb.CellObject;

/**
 * Contains methods
 *  
 */
public abstract class Drop extends CellObject {
	public abstract boolean isActive(long val);

	public abstract void reset(long time, int weight, float rate);

	public abstract Color getColor();

	public abstract boolean canStep();

	public void expire() {

	}

	public void onStep(ComplexAgent agent) {

	}
}