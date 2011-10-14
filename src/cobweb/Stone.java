package cobweb;


/**
 * A stone object.
 * @author Daniel Kats
 */
public class Stone extends CellObject {

	/**
	 * Create a new stone at the given position.
	 * @param position The location of the stone.
	 */
	public Stone(Environment.Location position) {
		this.position = position;
	}

	/**
	 * Cannot put anything on top of a stone.
	 * @return False.
	 */
	@Override
	public boolean canCoverWith(CellObject other) {
		//cannot place anything on top of a stone.
		return false;
	}
}
