package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;

/**
 * Modifies agents when one makes contact with another.
 */
public interface ContactMutator extends AgentMutator {

	/**
	 * Event called when an agent makes contact with another.
	 * @param bumper Agent that moved to make contact.
	 * @param bumpee Agent that got bumped into by the other.
	 */
	public void onContact(Agent bumper, Agent bumpee);
}
