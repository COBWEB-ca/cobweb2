package cwcore.complexParams;

import cwcore.ComplexAgent;

/**
 * Modifies agents when one makes contact with another.
 */
public interface ContactMutator extends AgentMutator {

	/**
	 * Event called when an agent makes contact with another.
	 * @param bumper Agent that moved to make contact.
	 * @param bumpee Agent that got bumped into by the other.
	 */
	public void onContact(ComplexAgent bumper, ComplexAgent bumpee);
}
