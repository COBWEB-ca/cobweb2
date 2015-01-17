package org.cobweb.cobweb2.core;

/**
 * Phenotype that does nothing
 */
public class NullPhenotype extends Phenotype {

	@Override
	public void modifyValue(Agent a, float m, float b) {
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

}
