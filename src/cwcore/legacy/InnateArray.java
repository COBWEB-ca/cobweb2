package cwcore.legacy;

public interface InnateArray extends Comparable<InnateArray>, Cloneable {
	/*
	 * Constructor: make an InnateArray to hold size objects. InnateArray(int
	 * size);
	 */

	/* Return an exact copy of this object. */
	Object clone();

	/*
	 * Return -1, 0, or 1, depending on whether this InnateArray is less than,
	 * equal to, or greater than other, by some measure.
	 */
	int compareTo(InnateArray other);

	/*
	 * Return a copy of this InnateArray, mutated to a degree determined by an
	 * internally-stored mutation rate.
	 */
	InnateArray copy(float mutationRate);

	/* Return the Object at index "i". */
	Object index(int i);

	/* Set the Object at index "i" to "o". */
	void index(int i, Object o);

	/* Give random values to all of the variables in the array. */
	void init();

	/* Return the number of elements this InnateArray can hold. */
	int size();

	/*
	 * "Genetically" splice two this InnateArray with other, returning a new
	 * InnateArray with approximately half of its information from each parent.
	 */
	InnateArray splice(InnateArray other);

	/*
	 * Return a unique String representation of this InnateArray. ie., two
	 * InnateArray's with identical properties will return the same String from
	 * their toString() methods.
	 */
	String toString();
}
