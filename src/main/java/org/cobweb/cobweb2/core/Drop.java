package org.cobweb.cobweb2.core;


/**
 * Contains methods
 *
 */
public interface Drop extends Updatable {
	public boolean canStep(Agent agent);

	public void onStep(Agent agent);

	public void prepareRemove();

	public int getProducerType();
}
