package org.cobweb.cobweb2.plugins.swarm;

import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;
import org.cobweb.util.MathUtil;


public class PairwiseEffect implements ParameterSerializable {

	/**
	 * Maximum distance between an agents considered to be part of the same swarm
	 */
	@ConfDisplayName("Radius")
	@ConfXMLTag("radius")
	public float radius = 0;

	/**
	 * Minimum number of nearby agents for effect to activate
	 */
	@ConfDisplayName("Group Size Min")
	@ConfXMLTag("countMin")
	public int countMin = 1;

	/**
	 * Number of agents for first peak of effect
	 */
	@ConfDisplayName("Group Size Peak 1")
	@ConfXMLTag("countPeak1")
	public int countPeak1 = 3;

	/**
	 * Effect at first peak
	 */
	@ConfDisplayName("Peak 1 Effect")
	@ConfXMLTag("factorPeak1")
	public float factorPeak1 = 1.1f;

	/**
	 * Number of agents for second peak of effect
	 */
	@ConfDisplayName("Group Size Peak 2")
	@ConfXMLTag("countPeak2")
	public int countPeak2 = 10;

	/**
	 * Effect at second peak
	 */
	@ConfDisplayName("Peak 2 Effect")
	@ConfXMLTag("factorPeak2")
	public float factorPeak2 = 1.2f;

	/**
	 * Number of nearby agents that causes the effect to deactivate
	 */
	@ConfDisplayName("Group Size Max")
	@ConfXMLTag("countMax")
	public int countMax = 15;

	/**
	 * Affected parameter
	 */
	@ConfDisplayName("Parameter")
	@ConfXMLTag("parameter")
	public Phenotype parameter = new NullPhenotype();

	/**
	 * Calculates effect factor for given group size
	 * @param groupSize number of nearby agents
	 * @return effect scale factor
	 */
	public float score(int groupSize) {
		if (groupSize < countMin || groupSize > countMax) {
			return 1.0f;
		} else if (groupSize < countPeak1) {
			return MathUtil.linearInterpolation(groupSize, countMin, countPeak1, 1.0f, factorPeak1);
		} else if (groupSize < countPeak2) {
			return MathUtil.linearInterpolation(groupSize, countPeak1, countPeak2, factorPeak1, factorPeak2);
		} else {
			return MathUtil.linearInterpolation(groupSize, countPeak2, countMax, factorPeak2, 1.0f);
		}
	}

	/**
	 * Scales effect score to the range [0, 1] for use in quantizing functions
	 * @param score score from score() function
	 * @return value of score relative to min/max of this effect
	 */
	public float relativeScore(float score) {
		float min = Math.min(1, Math.min(factorPeak1, factorPeak2));
		float max = Math.max(1, Math.max(factorPeak1, factorPeak2));
		return (score - min) / (max - min);
	}

	public void validate() throws IllegalArgumentException {
		if (countMin > countPeak1)
			throw new IllegalArgumentException("Swarm: Peak 1 should be greater or equal to Min");
		if (countPeak1 > countPeak2)
			throw new IllegalArgumentException("Swarm: Peak 2 should be greater or equal to Peak 1");
		if (countPeak2 > countMax)
			throw new IllegalArgumentException("Swarm: Max should be greater or equal to Peak 2");
		if (radius < 0)
			throw new IllegalArgumentException("Swarm: Radius should greater or equal to zero. Zero means no effect");
	}

	@Override
	protected PairwiseEffect clone() {
		try {
			PairwiseEffect copy = (PairwiseEffect) super.clone();
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
