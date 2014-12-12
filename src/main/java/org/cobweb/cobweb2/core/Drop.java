package org.cobweb.cobweb2.core;


/**
 * Contains methods
 *
 */
public interface Drop {
	public abstract boolean isActive(long val);

	public boolean canStep();

	public void expire();

	public void onStep(ComplexAgent agent);
}
