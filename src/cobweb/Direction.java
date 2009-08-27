package cobweb;

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
		v = new int[2];
		v[0] = x;
		v[1] = y;
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
}
