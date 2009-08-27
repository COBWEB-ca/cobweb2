package cwcore;

import cobweb.RandomNoGenerator;

/**
 * BehaviorArray is a class designed to take a collection of bits as input and for each permutation of those bits,
 * return a set of corresponding output bits. A set of output bits are stored for each permutation and thus the size of
 * this array grow exponentially with the number of input bits.
 *
 * Input: A collection of 6 bit groups combined into a single number: 1) 2 bits for first "seen" object (nothing, agent,
 * food, barrier) 2) 2 bits for distance of "seen" object 3) n bits for memory input (n equal to mem output from
 * previous timestep) 4) m bits for communication code received (m equal to communication from output) 5) 2 bits for
 * orientation (N(00),S(01),E(10),W(11)) 6) 2 bits for agent's current energy level == floor(E/40) and no more than 3
 * Output: A collection of 4 outputs: 1) 2 bits for physical action (0-left, 1-right, 2-stop, 3-step) 2) n bits for
 * memory output (n is user specified) 3) m bits for communication w/ other agents 4) 1 bit for sexual(0)/asexual(1)
 * reproduction
 *
 * In addition to associating a group of output bits with each permutation of input bits, BehaviorArray splits the
 * output into functional pieces, and during mutation and similarity operations it examines these pieces individually.
 *
 * BehaviorArray is extremely efficient in its storage of bits. It uses an array of integers but no single bit is
 * wasted. The amount of memory it consumes is approximately: outputBits*2^(inputBits-3) bytes
 *
 */

public class BehaviorArray {

	/**
	 * The following is obsolete code included here because they use techniques that might* be preferable to the current
	 * ones in use.
	 *
	 * The old version of copy mutated the output bit group as a whole and did not do it on the basis of the functional
	 * bit sub-groups.
	 *
	 * Like copy, similarity examined the whole output bit group for equality instead of the sub-groups.
	 *
	 * Below that is the old coloring algorithm might be interesting to look at. It is a fairly simple algorithm, made
	 * complex only because of the care it takes in dealing with floating-point precision limitations.
	 */

	/*
	 * public BehaviorArray copy(float mutationRate) { BehaviorArray newArray = new BehaviorArray( inputSize, outputSize
	 * ); for( int i = 0; i < size; ++i ) { if( cobweb.globals.random.nextFloat() <= mutationRate ) { newArray.set(i,
	 * cobweb.globals.random.nextBits(outputBits)); } else newArray.set(i, get(i) ); } return newArray; }
	 */
	/*
	 * public double similarity( BehaviorArray other ) { double total = 0; for( int i = 0; i < size; ++i ) { if( get(i)
	 * == other.get(i) ) total += 1.0; } return total/(double)size; }
	 */
	/*
	 * private static cobweb.ColorLookup colorMap = new cobweb.RbgInterpol(); private static final int addLim = 16;
	 * private float[] generateColor( int numComponents, int start, int end ) { int amount = end-start; float addArray[]
	 * = new float[numComponents]; if( amount <= addLim ) { float compArray[] = new float[numComponents]; for( int i =
	 * start; i < end; ++i ) { if( getBit(i) ) { colorMap.getColor(i,totalBits+1).getColorComponents( compArray ); for(
	 * int j = 0; j < numComponents; ++j ) { addArray[j] += compArray[j]; } } } for( int i = 0; i < numComponents; ++i )
	 * { addArray[i] /= amount; } } else { int numjump = (amount+addLim-1)/addLim; int jumpsize = amount/numjump; for(
	 * int i = start; i < end; i+=jumpsize ) { float getArray[] = generateColor( numComponents, i, i+jumpsize ); for(
	 * int j = 0; j < numComponents; ++j ) { addArray[j] += getArray[j]; } } if( amount%numjump != 0 ) { float
	 * getArray[] = generateColor( numComponents, end-jumpsize,end ); for( int j = 0; j < numComponents; ++j ) {
	 * addArray[j] += getArray[j]; } } for( int i = 0; i < numComponents; ++i ) { addArray[i] /= numjump; } } return
	 * addArray; } public java.awt.Color getColor() { //System.out.println(colorMap.getSpace().getNumComponents());
	 * float A[] = generateColor( colorMap.getSpace().getNumComponents(), 0, totalBits ); return new java.awt.Color(
	 * colorMap.getSpace(), A, 1.0f ).brighter().brighter(); }
	 */

	/*
	 * public java.awt.Color getColor() { float A[] = generateColor( 3, 0, totalBits ); return new java.awt.Color(
	 * A[0]2.0f, A[1]2.0f, A[2]2.0f ); //return new java.awt.Color( A[0], A[1], A[2]).brighter().brighter(); }
	 */

	private final int[] outputSize;

	private final int inputSize;

	private final int totalInMask;

	private final int totalOutMask;

	private int outputBits;

	private int totalInts;

	private final int[] array;

	private final int size;

	private final int totalBits;

	/**
	 * Constructor: Creates a behavior array that can accept an amount of input bits specified by the first parameter,
	 * and stores groups of output bits who's number and individual sizes are specified by the second parameter.
	 */
	public BehaviorArray(int input, int[] output) {

		/*
		 * Save basic information
		 */
		inputSize = input;
		outputSize = output;

		outputBits = 0;
		for (int i = 0; i < outputSize.length; ++i) {
			outputBits += outputSize[i];
		}

		if (inputSize > 32) {
			throw new java.lang.IllegalArgumentException("inputSize exceded 32");
		}
		if (outputBits > 32) {
			throw new java.lang.IllegalArgumentException("outputBits exceded 32");
		}
		/*
		 * Mask for all output bits in the form 0,1,2..,outputBits-1
		 */
		totalOutMask = (1 << (outputBits)) - 1;

		/*
		 * Mask for all output bits in the form 0,1,2..,inputSize-1
		 */
		totalInMask = (1 << (inputSize)) - 1;

		/*
		 * number of elements is the number of permutations for the input bits
		 */
		size = (1 << (inputSize));

		/*
		 * totalBits is equal to the output size times the number of permutations for the input bits
		 */
		totalBits = outputBits * size;

		/*
		 * totalInts is equal to the totalbits divided by 32 (there's 32 bits in an int) plus 1 since we might have a
		 * partial int used and the shift operation will round down. Certain methods also assume the existance of this
		 * extra int
		 */
		totalInts = (totalBits >> 5) + 1;
		if (totalInts == 1) {
			totalInts = 2;
		}

		array = new int[totalInts];

	}

	public long compareTo(Object other) {

		int[] otherArray = ((BehaviorArray) other).array;

		/*
		 * compare each int element is much faster than comparing each output element due to the overhead involved with
		 * the 'get' and 'set' methods
		 */
		for (int i = 0; i < totalInts; ++i) {
			if (array[i] < otherArray[i]) {
				return -1;
			}else if (array[i] > otherArray[i]) {
				return 1;
			}
		}
		return 0;
	}

	/* This method has been modified. It is now unaffected by mutationRate */
	public BehaviorArray copy(float mutationRate) {
		// TODO: should it mutate?

		BehaviorArray newArray = new BehaviorArray(inputSize, outputSize);
		int[] get = new int[outputSize.length];

		for (int i = 0; i < size; ++i) {
			getOutput(i, get);
			BitField outputCode = new BitField();

			for (int j = outputSize.length - 1; j >= 0; --j) {
				/***************************************************************
				 * *****This used to make the mutation based on behaviour array if( cobweb.globals.random.nextFloat() <=
				 * mutationRate ) { outputCode.add( cobweb.globals.random.nextBits( outputSize[j] ), outputSize[j] ); }
				 * else { outputCode.add( get[j], outputSize[j] ); }
				 */


				/* Replaces the above commented-out codes from above */
				outputCode.add(get[j], outputSize[j]);

			}
			newArray.set(i, outputCode.intValue());
		}

		return newArray;
	}

	public int get(int index) {

		/* bitbase is the first bit of the target element */
		int bitbase = index * outputBits;
		/*
		 * base is the first int value of the array that contains bit(s) of the target element
		 */
		int base = bitbase >> 5;
		/*
		 * basemod is the number of the first bit of the target element in the int value
		 */
		int basemod = bitbase & 31;

		/*
		 * Extracts the element value from the appropriate two array elements.
		 */

		long buff = (array[base] & 0xFFFFFFFFL) | ((long) array[base + 1] << 32);
		return (int) ((buff >>> basemod) & totalOutMask);
	}

	public boolean getBit(int number) {
		return (array[number >> 5] & (1 << (number & 31))) != 0;
	}

	/**
	 * @return an array filled with the different outputs associated with the element number given as a parameter
	 */
	public int[] getOutput(int in) {
		int[] out = new int[outputSize.length];
		BitField outputCode = new BitField(get(in & totalInMask));
		for (int i = 0; i < outputSize.length; ++i) {
			out[i] = outputCode.remove(outputSize[i]);
		}
		return out;
	}

	public void getOutput(int in, int[] out) {
		BitField outputCode = new BitField(get(in & totalInMask));
		for (int i = 0; i < outputSize.length; ++i) {
			out[i] = outputCode.remove(outputSize[i]);
		}
	}

	public int getSize() {
		return size;
	}

	/**
	 * fills the array with random bits one integer at a time
	 * @param seed Random seed to use
	 */
	public void randomInit(long seed) {

		RandomNoGenerator rng = new RandomNoGenerator(seed);
		for (int i = 0; i < totalInts; ++i) {
			array[i] = rng.nextInt();
		}

	}

	public void set(int index, int value) {

		/* bitbase is the first bit of the target element */
		int bitbase = index * outputBits;
		/*
		 * base is the first int value of the array that contains bit(s) of the target element
		 */
		int base = bitbase >> 5;
		/*
		 * basemod is the number of the first bit of the target element in the int value
		 */
		int basemod = bitbase & 31;
		/* mask off any excess bits */
		value &= totalInMask;

		/*
		 * Stores the element value to the appropriate two array elements.
		 */
		long buff = (array[base] & 0xFFFFFFFFL) | ((long) array[base + 1] << 32);

		buff &= ~((long) totalOutMask << basemod);
		buff |= (long) value << basemod;

		array[base] = (int) buff;
		array[base + 1] = (int) (buff >>> 32);
	}

	public double similarity(BehaviorArray other) {

		double total = 0;
		int cmpsize = size;
		if (size > other.getSize()) {
			cmpsize = other.getSize();
		}

		for (int i = 0; i < cmpsize; ++i) {
			BitField outputCode1 = new BitField(get(i));
			BitField outputCode2 = new BitField(other.get(i));
			for (int j = 0; j < outputSize.length; ++j) {
				if (outputCode1.remove(outputSize[j]) == outputCode2.remove(outputSize[j])) {
					total += outputSize[j];
				}
			}
		}
		return total / (size * outputBits);
	}

	public double similarity(int other) {

		double total = 0;
		int cmpsize = size;

		for (int i = 0; i < cmpsize; ++i) {
			BitField outputCode1 = new BitField(get(i));
			BitField outputCode2 = new BitField(other);
			for (int j = 0; j < outputSize.length; ++j) {
				if (outputCode1.remove(outputSize[j]) == outputCode2.remove(outputSize[j])) {
					total += outputSize[j];
				}
			}
		}
		return total / (size * outputBits);
	}

	/**
	 * @return the size of the behavior array in elements (should always be 1<<inputbits)
	 */
	public int size() {
		return size;
	}

	/**
	 * Splice takes another behavior array as a parameter and genetically splices it with this one to create a new
	 * behavior array. The algorithm is random and ensures that exactly half of the genetic makeup of each parent is
	 * used.
	 *
	 * Preconditions: BOther must be identical to this one in terms of input bits and output bits.
	 *
	 * @return a new behavior that is a splice of this one and the parameter
	 */

	public BehaviorArray splice(BehaviorArray BOther) {

		boolean[] boolArray = new boolean[size]; // initialized to false by
		// java

		BehaviorArray newBArray = new BehaviorArray(inputSize, outputSize);

		/*
		 * Fills boolArray up in a manner such that a random half of the elements are 'true'
		 */
		for (int i = 0;;) {

			int r = cobweb.globals.random.nextInt(size);
			if (boolArray[r] == false) {
				boolArray[r] = true;
				if (++i == size >> 1) {
					break;
				}
			}
		}
		/*
		 * Set the values of our new array according the values in boolArray. An element is from 'this' array if the
		 * corresponding element in boolArray is false and from the 'other' array (supplied as an arguement) if its true
		 */
		for (int i = 0; i < size; ++i) {

			if (boolArray[i]) {
				newBArray.set(i, BOther.get(i));
			} else {
				newBArray.set(i, get(i));
			}
		}

		return newBArray;
	}

}
