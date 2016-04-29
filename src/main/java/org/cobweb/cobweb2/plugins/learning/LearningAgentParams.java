package org.cobweb.cobweb2.plugins.learning;

import org.cobweb.io.ParameterSerializable;
import org.cobweb.util.CloneHelper;


public class LearningAgentParams implements ParameterSerializable {

	public LearningAgentParams() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public LearningAgentParams clone() {
		try {
			LearningAgentParams copy = (LearningAgentParams) super.clone();
			CloneHelper.resetMutatable(copy);
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
