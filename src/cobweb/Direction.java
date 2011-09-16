package cobweb;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/** Dimensionality independent notion of a direction */
public class Direction {

	public int[] v;

	public Direction(Direction dir) {
		v = dir.v.clone();
	}

	public Direction(int dim) {
		v = new int[dim];
	}

	public Direction(int x, int y) {
		v = new int[]{x, y};
	}

	public Direction(int[] initV) {
		v = initV;
	}

	/**
	 * Directions are equal when all their elements of v vector equal
	 * 
	 * @param other Direction to compare to
	 * @return true when directions equivalent
	 */
	public boolean equals(Direction other) {
		if (this.v.length != other.v.length) {
			throw new IllegalArgumentException("Other Direction of unequal dimension");
		}
		if (other == this)
			return true;
		for (int x = 0; x < v.length; x++) {
			if (this.v[x] != other.v[x])
				return false;
		}
		return true;
	}

	/**
	 * Flip the direction 180 degrees and return the new direction.
	 * @return This direction, flipped 180 degrees.
	 */
	public Direction flip() {
		return new Direction(-v[0], -v[1]);
	}

	/**
	 * Required for Comparable, just XOR all vector elements and shift the
	 * result at each XOR
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		for (int i : this.v)
			hash = (hash << 13 | hash >> 19) ^ i;
		return hash;
	}


	public void saveAsANode(Node node, Document doc){

		for (int i : v) {
			Element directionElement = doc.createElement("coordinate"); 
			directionElement.appendChild(doc.createTextNode(i +""));
			node.appendChild(directionElement);
		}

	}
}
