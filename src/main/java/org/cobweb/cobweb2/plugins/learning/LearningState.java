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

	@ConfXMLTag("AgentParams")
	LearningAgentParams agentParams;

	int cycleCounter;

	CircularFifoQueue<ControllerInput> inputMemory;
	CircularFifoQueue<ConsequenceGroup> consequesnces;
	ConsequenceGroup currentConsequence = new ConsequenceGroup();

	public LearningState(LearningAgentParams agentParams) {
		this.agentParams = agentParams;
		inputMemory = new CircularFifoQueue<>(agentParams.memorySteps);
		consequesnces = new CircularFifoQueue<>(agentParams.memorySteps);
		cycleCounter = agentParams.learningCycle;
	}

	@Override
	public void update() {
		if (!agentParams.learningEnabled)
			return;

		if (--cycleCounter < 0) {
			cycleCounter = agentParams.learningCycle;

			float weight = 1;
			float score = 0;

			ControllerInput worstDecision = null;
			float worstScore = Float.MAX_VALUE;

			// Find decision that caused the worst energy loss
			for (int t = inputMemory.size() - 1; t >= 0; t--) {
				ControllerInput input = inputMemory.get(t);
				for (int dt = t; dt >=0; dt-- ) {
					ConsequenceGroup c = consequesnces.get(dt);
					score += c.score() * weight;
				}

				if (score < worstScore || worstDecision == null) {
					worstDecision = input;
					worstScore = score;
				}

				weight *= (1 - agentParams.weighting);
			}

			// Randomly modify controller output for worst input
			worstDecision.mutate(agentParams.adjustmentStrength);

		}
	}

	public void recordInput(ControllerInput cInput) {
		if (!agentParams.learningEnabled)
			return;

		inputMemory.add(cInput);
		currentConsequence = new ConsequenceGroup();
		consequesnces.add(currentConsequence);
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
