package org.cobweb.cobweb2.core;

/**
 * Phenotype that does nothing
 */
public class NullPhenotype extends Phenotype {

	@Override
	public void modifyValue(Object cause, Agent a, float m) {
		// nothing
	}

	@Override
	public String getIdentifier() {
		return "None";
	}

	@Override
	public String getName() {
		return "[Null]";
	}

	private static final long serialVersionUID = 2L;

}
