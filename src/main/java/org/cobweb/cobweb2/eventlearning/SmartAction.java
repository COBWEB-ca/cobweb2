package org.cobweb.cobweb2.eventlearning;

import java.util.ArrayList;


public abstract class SmartAction implements Queueable {

	String desc;
	boolean isIrrelevant = false;
	ComplexAgentLearning agent;

	public SmartAction(ComplexAgentLearning agent) {
		this(agent, "default");
	}

	/**
	 * Create a new action with no information other than a description
	 * 
	 * @param agent the target agent
	 * @param desc a String to describe the action
	 */
	public SmartAction(ComplexAgentLearning agent, String desc) {
		this.desc = desc;
		this.agent = agent;
	}

	@Override
	public String getDescription() {
		return desc;
	}

	/**
	 * @return true if the action is no longer relevant (if it has already
	 *         been performed.)
	 */
	boolean irrelevantIfActionPerformed() {
		return true;
	}

	boolean irrelevantIfActionFailed() {
		return true;
	}

	/**
	 * @param event an event that may or may not be relevant
	 * @return true if the parameter event is relevant to this action
	 */
	public boolean eventIsRelated(MemorableEvent event) {
		return desc.equals(event.desc);
	}

	/*
	 * @return a list of all events related to this action
	 */
	final ArrayList<MemorableEvent> getRelatedEvents() {
		ArrayList<MemorableEvent> ret = new ArrayList<MemorableEvent>();
		if (getAgent().memEvents != null) {
			for (MemorableEvent me : getAgent().memEvents) {
				if (eventIsRelated(me)) {
					ret.add(me);
				}
			}
		}
		return ret;
	}

	/**
	 * @return whether or not this action ought to be performed By default
	 *         returns true if totalMagnitude >= 0
	 */
	public boolean actionIsDesireable() {
		return totalMagnitude() >= 0;
	}

	/**
	 * @return the sum of the magnitudes of all related events
	 */
	public final float totalMagnitude() {
		float ret = 0;

		for (MemorableEvent me : getRelatedEvents()) {
			ret += getMagnitudeFromEvent(me);
		}

		return ret;
	}

	public float getMagnitudeFromEvent(MemorableEvent event) {
		return event.getMagnitude();
	}

	/**
	 * The action that the agent is questioning whether or not to perform
	 */
	public abstract void desiredAction(ComplexAgentLearning agent);

	/**
	 * What to do if the wantedAction() is undesireable. By default, cancels
	 * the action from ever being performed (by stating isIrrelevant =
	 * false)
	 */
	public void actionIfUndesireable() {
	}

	ComplexAgentLearning getAgent() {
		return agent;
	}

	/**
	 * Performs desiredAction if it is a desireable thing to do. This is the
	 * method called by the agent.
	 * 
	 * desiredAction() will always be called if ignoreLearning = true.
	 */
	@Override
	public final void happen() {

		if (!agent.lParams.shouldLearn || actionIsDesireable()) {
			desiredAction(getAgent());
			if (!agent.lParams.shouldLearn || irrelevantIfActionPerformed()) {
				isIrrelevant = true;
			}
		} else {
			actionIfUndesireable();
			if (irrelevantIfActionFailed()) {
				isIrrelevant = true;
			}
		}
	}

	@Override
	public boolean isComplete() {
		return isIrrelevant;
	}
}