/**
 *
 */
package cobweb;

/**
 * Grid positions.  Can be used for both directions and positions.
 * 
 * <p>Direction examples (do not necessarily need to be normalized - 
 * only sign considered):
 * <br>North = (0, 1)
 * <br>South = (0, -1)
 * <br>East = (1, 0)
 * <br>West = (0, -1) 
 */
public class Point2D {

	/** X grid coordinate. */
	int x;

	/** Y grid coordinate. */
	int y;

	public Point2D() {
		x = 0;
		y = 0;
	}

	public Point2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
