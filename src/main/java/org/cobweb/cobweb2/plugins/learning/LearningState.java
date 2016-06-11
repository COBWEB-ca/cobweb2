package org.cobweb.cobweb2.plugins.learning;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.ControllerInput;
import org.cobweb.cobweb2.core.Updatable;
import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;


public class LearningState implements AgentState, Updatable {

	private LearningAgentParams agentParams;

	public LearningAgentParams getAgentParams() {
		return agentParams;
	}


	@ConfXMLTag("AgentParams")
	public void setAgentParams(LearningAgentParams agentParams) {
		this.agentParams = agentParams;

		// Number of steps in learning cycle
		cycleLength = agentParams.learningCycle.getValue();
		// Remember input for every learning step
		inputMemory = new CircularFifoQueue<>(cycleLength);

		// Remember output for memorySteps steps after input step
		memorySteps = agentParams.memorySteps.getValue();
		consequences = new CircularFifoQueue<>(memorySteps + cycleLength);
		cycleCounter = cycleLength;
	}

	int memorySteps;
	int cycleLength;
	int cycleCounter;

	CircularFifoQueue<ControllerInput> inputMemory;
	CircularFifoQueue<ConsequenceGroup> consequences;
	ConsequenceGroup currentConsequence = new ConsequenceGroup();

	@Deprecated // for reflection use only!
	public LearningState() {

	}

	public LearningState(LearningAgentParams agentParams) {
		setAgentParams(agentParams);
	}

	@Override
	public void update() {
		if (!agentParams.learningEnabled)
			return;

		if (--cycleCounter < 0) {
			cycleCounter = cycleLength;

			ControllerInput worstDecision = null;
			float worstScore = Float.MAX_VALUE;

			// Find decision that caused the worst energy loss
			for (int t = 0; t < inputMemory.size(); t++) {
				ControllerInput input = inputMemory.get(t);
				float weight = 1;
				float score = 0;

				// Don't go past end of array before memory is filled
				int newestConsequence = Math.min(t + memorySteps, consequences.size() - 1);

				for (int ct = newestConsequence; ct >= t; ct--) {
					ConsequenceGroup c = consequences.get(ct);
					score += c.score() * weight;
					weight *= (1 - agentParams.weighting.getValue());
				}

				if (score < worstScore || worstDecision == null) {
					worstDecision = input;
					worstScore = score;
				}

			}

			// Randomly modify controller output for worst input
			worstDecision.mutate(agentParams.adjustmentStrength.getValue());

		}
	}

	public void recordInput(ControllerInput cInput) {
		if (!agentParams.learningEnabled)
			return;

		inputMemory.add(cInput);
		currentConsequence = new ConsequenceGroup();
		consequences.add(currentConsequence);
	}

	public void recordChange(int delta, Cause cause) {
		if (!agentParams.learningEnabled)
			return;

		Consequence consequence = new Consequence(delta, cause);
		currentConsequence.addConsequence(consequence);
	}


	private static class ConsequenceGroup {
		List<Consequence> consequences = new ArrayList<>();

		public void addConsequence(Consequence c) {
			consequences.add(c);
		}

		public float score() {
			float score = 0;
			for (Consequence c : consequences) {
				score += c.delta;
			}
			return score;
		}
	}

	private static class Consequence {
		public Consequence(int delta, Cause cause) {
			this.delta = delta;
			this.cause = cause;
		}
		public int delta;

		@SuppressWarnings("unused")
		//TODO: more advanced learning can use this in the future
		public Cause cause;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	private static final long serialVersionUID = 1L;
}
