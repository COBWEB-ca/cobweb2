package org.cobweb.cobweb2.genetics;

import java.util.BitSet;
import java.util.Random;

/** The class that handles the core functionality of the
 * genetic algorithm. Aside from storing the actual
 * genetic sequence, it harbours a variety of operations
 * that analyses and changes the genetic sequence in
 * specific ways.
 */
public class GeneticCode {

	/**
	 * Compares two input bit strings of identical length and returns their %
	 * similarity. The similarity between the two string is determined solely by
	 * the number of identical digits they have and does not take into account
	 * of frame-shifts. Throws an exception if inputs are not identical in
	 * length or are inappropriate.
	 *
	 * This operation does not check for non-binary numeric strings.
	 *
	 * @return similarity between "genes1" and "genes2" range: 0.0 - 1.0
	 */
	public static float compareGeneticSimilarity(GeneticCode gc1, GeneticCode gc2) {

		if ((gc1 == null && gc2 != null) || (gc1 != null && gc2 == null))
			return 0;

		if (gc1 == null || gc2 == null)
			return 1;

		if (gc1 == gc2)
			return 1;



		int similarity_number = 0;
		int length = Math.min(gc1.bytes * 8, gc2.bytes * 8);
		int maxLen = Math.max(gc1.bytes * 8, gc2.bytes * 8);

		/*
		 * Go through each digit of the bit strings and add 1 to
		 * "similarity_number" for every matching digit.
		 */
		for (int i = 0; i < length; i++) {
			if (gc1.genes.get(i) == gc2.genes.get(i))
				similarity_number++;
		}
		if (maxLen == 0)
			return 1;

		return (float) similarity_number / maxLen;
	}

	/**
	 * Creates a new bit string based on two parent bit strings, "genes1" and
	 * "genes2". The new string is the binary representation of the parents'
	 * average decimal rgb values (Preliminary decision).
	 *
	 * @param genes1 Genes of parent 1
	 * @param genes2 Genes of parent 2
	 * @return The new bit string.
	 */
	public static GeneticCode createGeneticCodeMeiosisAverage(GeneticCode genes1, GeneticCode genes2) {
		GeneticCode result = new GeneticCode(genes1.bytes);
		for (int i = 0; i < result.bytes; i++) {
			int s = genes1.getByte(i * 8) + genes2.getByte(i * 8);
			result.setByte(i * 8, (byte)(s / 2));
		}
		return result;
	}

	/**
	 * Creates a new bit string based on two parent bit strings, "genes1" and
	 * "genes2". Each gene encoded in the new string will be randomly
	 * taken from one of the parents. A minimum of one gene must be
	 * inherited from each parent.
	 *
	 * @param genes1 Genes of parent 1
	 * @param genes2 Genes of parent 2
	 * @return The new bit string.
	 */
	public static GeneticCode createGeneticCodeMeiosisGeneSwap(GeneticCode genes1, GeneticCode genes2, Random random) {
		GeneticCode result = new GeneticCode(genes1.bytes);
		for (int i = 0; i < result.bytes; i++) {
			byte s;
			if (random.nextBoolean())
				s = genes1.getByte(i * 8);
			else
				s = genes2.getByte(i * 8);

			result.setByte(i * 8, s);
		}
		return result;
	}

	/**
	 * Creates a new bit string based on two parent bit strings, "genes1" and
	 * "genes2". The new string is the combination of one fragment
	 * from each parent (total length is still 24).
	 *
	 * @param genes1 Genes of parent 1
	 * @param genes2 Genes of parent 2
	 * @return The new bit string.
	 */
	public static GeneticCode createGeneticCodeMeiosisRecomb(GeneticCode genes1, GeneticCode genes2, Random random) {
		assert(genes1.bytes == genes2.bytes);
		GeneticCode result = new GeneticCode(genes1.bytes);
		int split = random.nextInt(result.bytes * 8);

		for (int i = 0; i < split; i++)
			result.genes.set(i, genes1.genes.get(i));

		for (int i = split; i < genes1.bytes * 8; i++)
			result.genes.set(i, genes2.genes.get(i));

		return result;
	}

	private BitSet genes;

	private int bytes;

	public GeneticCode(GeneticCode parent) {
		this(parent.bytes);
		genes.or(parent.genes);
	}

	public GeneticCode(int bytes) {
		this.bytes = bytes;
		genes = new BitSet(bytes * 8);
	}

	public void bitsFromString(int start, int length, String string, int stringStart) {
		int len = Math.min(length, string.length() - stringStart);
		for (int i = 0; i < len; i++) {
			genes.set(start + i, string.charAt(stringStart + (len - i - 1)) == '1');
		}
	}

	private byte getByte(int position) {
		byte res = 0;
		for (int i = 0; i < 8; i++) {
			res = (byte)(res << 1);
			boolean on = genes.get(position + (7 - i));
			res += on ? 1 : 0;
		}
		return res;
	}

	public int getNumGenes() {
		return bytes;
	}

	public float getStatus(int gene) {
		return 2 * (float)Math.abs(Math.sin(getValue(gene) * Math.PI / 180));
	}

	/**
	 * Returns the rgb colour associated to global instance variable "genes".
	 *
	 * @return An int array that stores the rgb colour.
	 */
	public int getValue(int gene) {
		return getByte(gene * 8) & 0xff;
	}

	/**
	 * Mutates a bit string "genes" at a random bit and returns it.
	 *
	 * @param position The position of the string to be mutated.
	 */
	public void mutate(int position) {
		genes.flip(position);
	}

	private void setByte(int position, byte value) {
		for (int i = 0; i < 8; i++) {
			boolean on = (value & (1 << i)) > 0;
			genes.set(position + i, on);
		}
	}

	public void setValue(int gene, int value) {
		setByte(gene * 8, (byte)value);
	}

	public String stringFromBits(int start, int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = length - 1; i >= 0; i--) {
			sb.append(genes.get(start + i) ? '1' : '0');
		}
		return sb.toString();
	}
}
