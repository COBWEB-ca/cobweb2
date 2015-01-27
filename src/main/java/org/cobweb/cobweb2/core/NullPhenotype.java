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

	private static final long serialVersionUID = 2L;

	@Override
	public float getValue(Agent a) {
		return 0;
	}

	@Override
	public void setValue(Agent a, float value) {
		// nothing
	}
}
