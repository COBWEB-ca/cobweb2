package cwcore;

import java.awt.Color;

import cobweb.CellObject;

/**
 * Contains methods
 *  
 */
public abstract class Drop extends CellObject {
	//Gold colored drops
	final static Color DROP_COLOR = new Color(238, 201, 0);

	public Drop() {

	}

	public abstract boolean isActive(long val);

	public abstract void reset(long time, int weight, float rate);

	public Color getColor() {
		return DROP_COLOR;
	}

	public boolean canStep() {
		return true;
	}
}