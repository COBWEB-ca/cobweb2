package org.cobweb.cobweb2.core;

import org.cobweb.io.ParameterChoice;

/**
 * Property of an agent that can be modified
 */
public abstract class Phenotype implements ParameterChoice {

	/**
	 * Modifies Phenotype of agent using linear transformation formula
	 * <code> p' = p * m + b</code>
	 * @param a Agent to modify
	 * @param m scale factor
	 * @param b offset factor
	 */
	public abstract void modifyValue(Agent a, float m, float b);

	@Override
	public String toString() {
		return getName();
	}

	private static final long serialVersionUID = 2L;
}
