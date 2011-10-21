package cobweb;



/**
 * Any object occupying a cell.
 * @author Daniel Kats
 */
public abstract class CellObject {

	protected Environment.Location position;

	/**
	 * Return true if this thing can move, false otherwise.
	 * @return True if the object can move, false otherwise.
	 */
	public abstract boolean canMove();

	/**
	 * Return true if the other object can be placed on top of this one.
	 * Return false otherwise.
	 * @param other Another cell object.
	 * @return True if can place the other object on top of this one.
	 */
	public abstract boolean canCoverWith(CellObject other);

	/**
	 * Return the location of this object.
	 * @return The location of this object.
	 */
	public final Environment.Location getLocation() {
		return position;
	}
}
