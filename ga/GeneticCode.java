package ga;

/** The class that handles the core functionality of the
 * genetic algorithm. Aside from storing the actual
 * genetic sequence, it harbours a variety of operations
 * that analyses and changes the genetic sequence in
 * specific ways.
 */
public class GeneticCode {

	/** The number of bits the genetic code is represented by. */
	public static final int NUM_BITS = 24;

	/**
	 * The starting position in the genetic code coding for the red pixel value
	 * of the agent's skin colour.
	 */
	public static final int STARTING_BIT_RED = 0;

	/**
	 * The starting position in the genetic code coding for the green pixel
	 * value of the agent's skin colour.
	 */
	public static final int STARTING_BIT_GREEN = 8;

	/**
	 * The starting position in the genetic code coding for the blue pixel value
	 * of the agent's skin colour.
	 */
	public static final int STARTING_BIT_BLUE = 16;

	/** The number of bits in the genetic code coding for each colour. */
	public static final int BITS_PER_COLOUR = 8;

	/** The column that stores red pixel values in "rgb_pixel". */
	public static final int RED_PIXEL_COLUMN = 0;

	/** The column that stores green pixel values in "rgb_pixel". */
	public static final int GREEN_PIXEL_COLUMN = 1;

	/** The column that stores blue pixel values in "rgb_pixel". */
	public static final int BLUE_PIXEL_COLUMN = 2;

	/** Number of fundamental colours. RGB = 3. */
	public static final int NUM_COLOURS = 3;

	/** Number of genes in genome.  */
	public static final int NUM_GENES = 3;

	/**
	 * The genetic code of an agent represented by a bit String of 0's and 1's
	 * with length specified by NUM_BITS (Default is 24).
	 */
	private String genes;

	/**
	 * The RGB pixel value directly linked to "genes" represented by a size 3
	 * integer array. The red, green, and blue pixel values are stored in
	 * columns 0, 1, and 2, respectively.
	 */
	private int[] rgb_value = new int[3];

	/**
	 * Determines which mode of meiosis this organism uses.
	 * "Colour Averaging" -> The int-equivalent values of
	 * 						 all three genes are averaged
	 *                       from both parents.
	 * "Random Recombination" -> The genetic code is the
	 *                           combination of one continuous
	 *                           fragment of each parent.
	 * "Gene Swapping" -> Two genes are taken from one parent
	 *                    and one gene is taken from the
	 *                    other parent.
	 */
	public static String meiosis_mode = "Colour Averaging";

	/* The numerical representation of meiosis_mode variable.
	 * 0 = Colour Averaging (Default)
	 * 1 = Random Recombination
	 * 2 = Gene Swapping
	 */
	public static int meiosis_mode_index = 0;
	// $$$$$$ add constant DEFAULT_MEIOSIS_MODE_INDEX.  Jan 31
	public static final int DEFAULT_MEIOSIS_MODE_INDEX = 0;

	/**
	 * Constructor method of the GeneticCode object. Takes in a string of binary
	 * digits of length equivalent to NUM_BITS and stores it in the instance
	 * global variable "genes". Throws GeneticCodeException if the input's
	 * length is not equivalent to NUM_BITS.
	 *
	 * @param bits
	 *            The bit string to be used as the genetic sequence.
	 * @throws GeneticCodeException
	 */
	public GeneticCode(String bits) throws GeneticCodeException {
		if (bits.length() != NUM_BITS) { // If the bit string has
											// inappropriate length.
			throw new GeneticCodeException(
					"Constructor: Inappropriate input length - " + bits);
		} else {
			genes = bits; // Store the bit string.
		}
		obtainGeneticColour(); // Get the colour assoiciated to the bit string.
	}

	/**
	 * Derives rgb values from the global variable "genes". The first
	 * "BITS_PER_COLOUR" of bits starting at bit number "STARTING_BIT_RED" in
	 * "genes" will represent the red colour value. Likewise for green and blue.
	 * This method is privately called exclusively by the constructor.
	 *
	 * @throws GeneticCodeException
	 */
	private void obtainGeneticColour() throws GeneticCodeException {
		// Derive rgb colour values from "genes", a 24-bit string.
		String red_pixel_value = genes.substring(STARTING_BIT_RED,
				STARTING_BIT_RED + BITS_PER_COLOUR);
		String green_pixel_value = genes.substring(STARTING_BIT_GREEN,
				STARTING_BIT_GREEN + BITS_PER_COLOUR);
		String blue_pixel_value = genes.substring(STARTING_BIT_BLUE,
				STARTING_BIT_BLUE + BITS_PER_COLOUR);
		rgb_value[RED_PIXEL_COLUMN] = byteToInt(red_pixel_value);
		rgb_value[GREEN_PIXEL_COLUMN] = byteToInt(green_pixel_value);
		rgb_value[BLUE_PIXEL_COLUMN] = byteToInt(blue_pixel_value);
	}

	/**
	 * Converts a bit string into an decimal integer value. The bit string can
	 * be of length 0 to 9. GeneticCodeException is thrown if "bit" exceeds
	 * maximum length or is not a binary string.
	 *
	 * @param bit
	 *            A string of binary number to be converted into a decimal
	 *            integer
	 * @return The equivalent decimal integer of "bit".
	 * @throws GeneticCodeException
	 */
	public static int byteToInt(String bit) throws GeneticCodeException {
		int bit_length = bit.length();
		if (bit_length > 9) { // Disallow input with length > 9.
			throw new GeneticCodeException("byteToInt: Input length exceeds 9.");
		}
		int decimal_value = 0;
		try {
			for (int i = 0; i < bit_length; i++) {
				int digit = Integer.parseInt(bit.substring(i, i + 1));
				if (digit != 0 && digit != 1) { // Throw exception if input is
												// not binary.
					throw new GeneticCodeException(
							"byteToInt: Non-binary digits detected.");
				}
				decimal_value += digit * Math.pow(2, (bit_length - i - 1));
			}
		} catch (NumberFormatException e) { // Throw exception if input is not
											// numeric.
			throw new GeneticCodeException(
					"byteToInt: Inappropriate input format.");
		}
		return decimal_value;
	}

	/**
	 * Converts a decimal integer "value" to a binary string. The bit string can
	 * be of length 0 to 9. GeneticCodeException is thrown if "length" > 9 or if
	 * integer is not representable by a bit string of specified length.
	 *
	 * @param value
	 *            A decimal integer.
	 * @param length
	 *            Denotes the length of the bit string.
	 * @return The bit string representation "value".
	 * @throws GeneticCodeException
	 */
	public static String intToByte(int value, int length)
			throws GeneticCodeException {
		String bit = "";
		if (length > 9) { // Disallow input with length > 9.
			throw new GeneticCodeException("intToByte: Length exceeds 9.");
		} else if (value >= Math.pow(2, length)) {
			throw new GeneticCodeException(
					"intToByte: Integer cannot be represented.");
		}
		for (int i = 0; i < length; i++) {
			int num = (int) Math.pow(2, length - i - 1);
			if (value >= num) {
				value -= num;
				bit += "1";
			} else {
				bit += "0";
			}
		}
		return bit;
	}

	/**
	 * Returns the rgb colour associated to global instance variable "genes".
	 *
	 * @return An int array that stores the rgb colour.
	 */
	public int[] getGeneticColour() {
		return rgb_value;
	}

	/**
	 * Returns the bit string that stores the genetic code.
	 *
	 * @return A bit string that stores the genetic code.
	 */
	public String getGeneticInfo() {
		return genes;
	}

	/**
	 * Compares two input bit strings of identical length and returns their %
	 * similarity. The similarity between the two string is determined solely by
	 * the number of identical digits they have and does not take into account
	 * of frame-shifts. Throws an exception if inputs are not identical in
	 * length or are inappropriate.
	 *
	 * This operation does not check for non-binary numeric strings.
	 *
	 * @param genes1
	 *            An input bit string.
	 * @param genes2
	 *            Another input bit string.
	 * @return The % similarity between "genes1" and "genes2"
	 * @throws GeneticCodeException.
	 */
	public static double compareGeneticSimilarity(String genes1, String genes2)
			throws GeneticCodeException {
		int similarity_number = 0; // Number of identical bits.
		int length = Math.max(genes1.length(), genes2.length()); // Length of
																	// the
																	// longest
																	// bit
																	// string.
		try {
			/*
			 * Go through each digit of the bit strings and add 1 to
			 * "similarity_number" for every matching digit.
			 */
			for (int i = 0; i < length; i++) {
				if (Integer.parseInt(genes1.substring(i, i + 1)) == Integer
						.parseInt(genes2.substring(i, i + 1))) {
					similarity_number++;
				}
			}
		} catch (StringIndexOutOfBoundsException e) { // Throw exception if
														// inputs are not of
														// same length
			throw new GeneticCodeException(
					"compareGeneticSimilarity: Inputs are not of identical length");
		} catch (NumberFormatException e) { // Throw exception if input is not
											// numeric.
			throw new GeneticCodeException(
					"compareGeneticSimilarity: Inappropriate input format.");
		}
		return similarity_number / length;
	}

	public static String createGeneticCodeMeiosis(String genes1, String genes2)
		throws GeneticCodeException {
		String new_genes = "";
		if (meiosis_mode.equals("Colour Averaging")) {
			new_genes = createGeneticCodeMeiosisAverage(genes1,genes2);
		} else if (meiosis_mode.equals("Random Recombination")) {
			new_genes = createGeneticCodeMeiosisRecomb(genes1,genes2);
		} else if (meiosis_mode.equals("Gene Swapping")) {
			new_genes = createGeneticCodeMeiosisGeneSwap(genes1,genes2);
		} else {
			throw new GeneticCodeException ("createGeneticCodeMeiosis: Invalid meiosis mode - " + meiosis_mode);
		}
	return new_genes;
	}


	/**
	 * Creates a new bit string based on two parent bit strings, "genes1" and
	 * "genes2". The new string is the binary representation of the parents'
	 * average decimal rgb values (Preliminary decision).
	 *
	 * @param genes1
	 *            A parent of the new bit string.
	 * @param genes2
	 *            Another parent of the new bit string.
	 * @return The new bit string.
	 * @throws GeneticCodeException
	 */
	public static String createGeneticCodeMeiosisAverage(String genes1, String genes2)
			throws GeneticCodeException {
		String new_genes = "";
		try {
			// The new bit string is the average the rgb values of the parents.
			for (int i = 0; i < NUM_BITS; i += BITS_PER_COLOUR) {
				int parent_colour_1 = GeneticCode.byteToInt(genes1.substring(i,
						i + BITS_PER_COLOUR));
				int parent_colour_2 = GeneticCode.byteToInt(genes2.substring(i,
						i + BITS_PER_COLOUR));

				// Add the decimal-averaged bits to the new bit string.
				new_genes += intToByte((parent_colour_1 + parent_colour_2) / 2,
						BITS_PER_COLOUR);
			}
		} catch (StringIndexOutOfBoundsException e) { // Throw exception if
														// inputs are not of
														// same length
			throw new GeneticCodeException(
					"createGeneticCodeMeiosis: Inappropriate input length.");
		} catch (NumberFormatException e) { // Throw exception if input is not
											// numeric.
			throw new GeneticCodeException(
					"compareGeneticSimilarity: Inappropriate input format.");
		}
		return new_genes;
	}

	/**
	 * Creates a new bit string based on two parent bit strings, "genes1" and
	 * "genes2". The new string is the combination of one fragment
	 * from each parent (total length is still 24).
	 *
	 * @param genes1
	 *            A parent of the new bit string.
	 * @param genes2
	 *            Another parent of the new bit string.
	 * @return The new bit string.
	 * @throws GeneticCodeException
	 */
	public static String createGeneticCodeMeiosisRecomb(String genes1, String genes2)
			throws GeneticCodeException {
		String new_genes = "";
		try {
			/* Randomly choose a position where bits before
			 * that come from first parent and bits at and
			 * after that are from second parent.
			 */
			int position = cobweb.globals.random.nextInt(NUM_BITS);
			new_genes = genes1.substring(0,position)
				+ genes2.substring(position);
		} catch (StringIndexOutOfBoundsException e) { // Throw exception if
														// inputs are not of
														// same length
			throw new GeneticCodeException(
					"createGeneticCodeMeiosis: Inappropriate input length.");
		} catch (NumberFormatException e) { // Throw exception if input is not
											// numeric.
			throw new GeneticCodeException(
					"compareGeneticSimilarity: Inappropriate input format.");
		}
		return new_genes;
	}

	/**
	 * Creates a new bit string based on two parent bit strings, "genes1" and
	 * "genes2". Each gene encoded in the new string will be randomly
	 * taken from one of the parents. A minimum of one gene must be
	 * inherited from each parent.
	 *
	 * @param genes1
	 *            A parent of the new bit string.
	 * @param genes2
	 *            Another parent of the new bit string.
	 * @return The new bit string.
	 * @throws GeneticCodeException
	 */
	public static String createGeneticCodeMeiosisGeneSwap(String genes1, String genes2)
			throws GeneticCodeException {
		String new_genes = "";
		try {
			int PARENT1 = 0;

			int parent1_contribution = 0;
			int parent2_contribution = 0;

			/* Randomly chooses a contributing parent for each
			 * gene and keeps track of how many genes are
			 * derived from each parent.
			 */
			for (int i = 0; i < NUM_BITS; i+= BITS_PER_COLOUR) {
				if (cobweb.globals.random.nextInt(2) == PARENT1) {
					new_genes += genes1.substring(i,i+BITS_PER_COLOUR);
					parent1_contribution++;
				} else {
					new_genes += genes2.substring(i,i+BITS_PER_COLOUR);
					parent2_contribution++;
				}
			}

			/* If one parent is not genetically contributing
			 * to the child's genetic code, sub in this
			 * parent's (blue-linked) third gene.
			 */
			if (parent1_contribution > 2) {
				new_genes = new_genes.substring(0, STARTING_BIT_BLUE)
				+ genes2.substring(STARTING_BIT_BLUE);
			} else if (parent2_contribution > 2) {
				new_genes = new_genes.substring(0, STARTING_BIT_BLUE)
				+ genes1.substring(STARTING_BIT_BLUE);
			}
		} catch (StringIndexOutOfBoundsException e) { // Throw exception if
														// inputs are not of
														// same length
			throw new GeneticCodeException(
					"createGeneticCodeMeiosis: Inappropriate input length.");
		} catch (NumberFormatException e) { // Throw exception if input is not
											// numeric.
			throw new GeneticCodeException(
					"compareGeneticSimilarity: Inappropriate input format.");
		}
		return new_genes;
	}

	/**
	 * Mutates a bit string "genes" at a random bit and returns it.
	 *
	 * @param seq
	 *            The bit string to be mutated.
	 * @param position
	 *            The position of the string to be mutated.
	 * @return The mutated bit string.
	 */
	public static String mutate(String seq, int position)
			throws GeneticCodeException {
		String new_genes = "";
		if (seq.length() != NUM_BITS) {
			throw new GeneticCodeException(
					"mutate: Inappropriate input length.");
		} else {
			try {
				new_genes += seq.substring(0, position);
				new_genes += Math.abs(Integer.parseInt(seq.substring(position,
						position + 1)) - 1);
				new_genes += seq.substring(position + 1);
			} catch (StringIndexOutOfBoundsException e) { // Throw exception
															// if position >
															// seq's length
				throw new GeneticCodeException(
						"mutate: Specified position is out of bound.");
			}
		}
		return new_genes;
	}
}
