package cobweb;

import java.util.Random;
import java.awt.*;
import java.awt.color.*;

public class RandomNoGenerator extends Random {

	private long seed;
	
	public RandomNoGenerator(long seed) {
		super(seed);
	}

	public long getSeed() {
		return seed;
	}
	
	/**
	 * @return a random integer over (m,n]
	 */
	public int nextIntRange(int m, int n) {
		return nextInt(n - m) + m;
	}

	/**
	 * @return an integer whose lower n bits are randomized
	 */
	public int nextBits(int n) {
		return next(n);
	}
}