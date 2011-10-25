package cell;

import java.awt.Color;

import cobweb.Location;

/**
 * A tile of blood. Blood is like water, except it's red.
 * @author Daniel Kats
 */
public class Blood extends Water {
	/**
	 * Create a new blood tile.
	 * @param coords Given coordinates.
	 */
	public Blood(Location coords) {
		super(coords);
		//blood is red
		this.color = Color.red;
	}
}
