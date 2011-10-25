package cell;

import java.awt.Color;

import cobweb.Environment.Location;

/**
 * A tile of blood. Blood is like water, except it's red.
 * @author Daniel Kats
 */
public class Blood extends Water {

	/**
	 * Blood is red.
	 * @return Color of blood.
	 */
	@Override
	public Color getColor() {
		return Color.red;
	}

	/**
	 * Create a new blood tile.
	 * @param coords Given coordinates.
	 */
	public Blood(Location coords) {
		super(coords);
	}
}
