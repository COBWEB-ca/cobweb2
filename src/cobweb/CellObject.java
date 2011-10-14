package cobweb;


/**
 * Any object occupying a cell.
 * @author Daniel Kats
 */
public abstract class CellObject {

	protected Environment.Location position;

	/**
	 * Return true if the other object can be placed on top of this one.
	 * Return false otherwise.
	 * @param other Another cell object.
	 * @return True if can place the other object on top of this one.
	 */
	public abstract boolean canPlaceOnTop(CellObject other);

	/**
	 * Return the location of this object.
	 * @return The location of this object.
	 */
	public final Environment.Location getLocation() {
		return position;
	}
}
