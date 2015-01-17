package org.cobweb.cobweb2.genetics;

public enum MeiosisMode {
	ColourAveraging("Colour Averaging"),
	RandomRecombination("Random Recombination"),
	GeneSwapping("Gene Swapping");

	/**
	 * User-friendly name for mode
	 */
	private final String value;

	private MeiosisMode(String s) {
		value = s;
	}

	/**
	 * Gets MeiosisMode from friendly name
	 * @param s friendly name
	 * @return MeiosisMode with given name
	 * @throws IllegalArgumentException when no MeiosisMode has given friendly name
	 */
	public static MeiosisMode fromString(String s) { // NO_UCD called through reflection
		for (MeiosisMode m : MeiosisMode.values()) {
			if (m.value.equals(s))
				return m;
		}
		throw new IllegalArgumentException("Invalid value");
	}

	@Override
	public String toString() {
		return value;
	}
}
