package cobweb;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/** 
 * Dimensionality independent notion of a direction.  The integer 
 * v is a vector representation of the direction, defined by the
 * environment. 
 */
public class Direction {

	public int[] v;

	/**
	 * Return the string representation of this direction.
	 * @return The string representation of this direction.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");

		for(int i = 0; i < v.length; i++) {
			if(i > 0) {
				builder.append(", ");
			}

			builder.append(Integer.toString(this.v[i]));
		}

		builder.append(")");

		return builder.toString();
	}

	/**
	 * Return a random cardinal direction.
	 * @return A random cardinal direction.
	 */
	public static Direction getRandom() {
		//we get 0, 1/2, 1, 1.5
		double random = Math.floor(cobweb.globals.random.nextFloat() * 4) / 2;

		int x = (int) Math.cos(Math.PI * random);
		int y = (int) Math.sin(Math.PI * random);

		return new Direction(x, y);
	}

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
	 * Return the angle (between -pi and pi) between the x and y coordinates
	 * of this direction vector. The angle is in standard position.
	 * @return An angle between -pi and pi.
	 */
	public final double angle() {
		return Math.atan2(v[1], v[0]);
	}

	/**
	 * Returns this direction, rotated 90 degrees to the left.
	 * @return The direction if the current direction was rotated to the left.
	 */
	public Direction rotateLeft() {
		double newAngle = this.angle() + (Math.PI  / 2);

		double x = Math.round(Math.cos(newAngle));
		double y = Math.round(Math.sin(newAngle));

		return new Direction((int) x, (int) y);
	}

	/**
	 * Returns this direction, rotated 90 degrees to the right.
	 * @return The direction if the current direction was rotated to the right.
	 */
	public Direction rotateRight() {
		double newAngle = this.angle() - (Math.PI / 2);

		double x = Math.round(Math.cos(newAngle));
		double y = Math.round(Math.sin(newAngle));

		return new Direction((int) x, (int) y);
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
