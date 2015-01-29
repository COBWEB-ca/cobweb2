package org.cobweb.cobweb2.plugins.genetics;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfSquishParent;

/** The class that handles the core functionality of the
 * genetic algorithm. Aside from storing the actual
 * genetic sequence, it harbours a variety of operations
 * that analyses and changes the genetic sequence in
 * specific ways.
 */
public class GeneticCode implements AgentState {

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

		int maxLen = Math.max(gc1.getNumGenes() * 8, gc2.getNumGenes() * 8);
		if (maxLen == 0)
			return 1;

		BitSet differentBits = gc1.getBitSet();
		differentBits.xor(gc1.getBitSet());

		int similarity_number = maxLen - differentBits.cardinality();

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
		GeneticCode result = new GeneticCode(genes1.getNumGenes());
		for (int i = 0; i < result.getNumGenes(); i++) {
			int s = genes1.getValue(i) + genes2.getValue(i);
			result.setValue(i, (byte)(s / 2));
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
		GeneticCode result = new GeneticCode(genes1.getNumGenes());
		for (int i = 0; i < result.getNumGenes(); i++) {
			int s;
			if (random.nextBoolean())
				s = genes1.getValue(i);
			else
				s = genes2.getValue(i);

			result.setValue(i, (byte) s);
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
		assert(genes1.getNumGenes() == genes2.getNumGenes());
		GeneticCode result = new GeneticCode(genes1.getNumGenes());
		int split = random.nextInt(result.getNumGenes() * 8);

		BitSet resultBS = genes1.getBitSet().get(0, split);
		BitSet rightSide = genes2.getBitSet();
		rightSide.clear(0, split);
		resultBS.or(rightSide);

		result.setBitSet(resultBS);

		return result;
	}

	@ConfSquishParent
	@ConfList(indexName="gene", startAtOne = true)
	public byte[] genes;

	public GeneticCode(GeneticCode parent) {
		this(parent.getNumGenes());
		genes = Arrays.copyOf(parent.genes, parent.getNumGenes());
	}

	public void bitsFromString(int start, int length, String string, int stringStart) {
		BitSet bs = getBitSet();
		int len = Math.min(length, string.length() - stringStart);
		for (int i = 0; i < len; i++) {
			bs.set(start + i, string.charAt(stringStart + (len - i - 1)) == '1');
		}
		setBitSet(bs);
	}

	public GeneticCode(int geneCount) {
		genes = new byte[geneCount];
	}

	public int getNumGenes() {
		return genes.length;
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
		return genes[gene] & 0xff;
	}

	public void setValue(int gene, byte value) {
		genes[gene] = value;
	}

	private BitSet getBitSet() {
		return BitSet.valueOf(genes);
	}

	private void setBitSet(BitSet bs) {
		genes = Arrays.copyOf(bs.toByteArray(), getNumGenes());
	}

	/**
	 * Mutates a bit string "genes" at a random bit and returns it.
	 *
	 * @param position The position of the string to be mutated.
	 */
	public void mutate(int position) {
		BitSet bs = getBitSet();
		bs.flip(position);
		setBitSet(bs);
	}

	public String stringFromBits(int start, int length) {
		BitSet bs = getBitSet();
		StringBuilder sb = new StringBuilder(length);
		for (int i = length - 1; i >= 0; i--) {
			sb.append(bs.get(start + i) ? '1' : '0');
		}
		return sb.toString();
	}

	@Override
	public boolean isTransient() {
		return false;
	}
	private static final long serialVersionUID = 1L;

}
