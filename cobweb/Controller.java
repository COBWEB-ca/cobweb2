package cobweb;

/**
 * The "brain" of an Agent, the controller causes the controlled agent to
 * act by calling methods on it. The Controller is notified by a call to
 * control agent that the Agent is requesting guidance.
 */
public interface Controller {
	/**
	 * Notification that a new agent is using this controller. It is
	 * concievable that multiple agents may use a single controller.
	 */
	public void addClientAgent(Agent theAgent);

	/**
	 * Remove an agent from the control of this Controller.
	 */
	public void removeClientAgent(Agent theAgent);

	/** Cause the specified agent to act. */
	public void controlAgent(Agent theAgent);

}