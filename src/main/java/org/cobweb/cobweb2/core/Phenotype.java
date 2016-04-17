package org.cobweb.cobweb2.core;

import org.cobweb.io.ParameterChoice;

/**
 * Property of an agent that can be modified
 */
public abstract class Phenotype implements ParameterChoice {

	/**
	 * Modifies Phenotype of agent using linear transformation formula
	 * <code> p' = p * m + b</code>
	 * @param cause Cause/source of modification. Used to keep track of multiple multipliers on the same phenotype
	 * @param a Agent to modify
	 * @param m scale factor
	 */
	public abstract void modifyValue(Object cause, Agent a, float m);

	/**
	 * Undoes any modification done by this cause
	 * @param cause Cause/source of modification. Used to keep track of multiple multipliers on the same phenotype
	 * @param a Agent to modify
	 */
	public abstract void unmodifyValue(Object cause, Agent a);

	@Override
	public String toString() {
		return getName();
	}

	private static final long serialVersionUID = 2L;
}
