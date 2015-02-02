package org.cobweb.cobweb2.impl.ai;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

/**
 * LinearWeightsController per-agent-type parameters
 */
public class LinearWeightAgentParam implements ParameterSerializable {

	@ConfDisplayName("Mutation Rate")
	@ConfXMLTag("MutationRate")
	public float mutationRate = 0.05f;

	private static final long serialVersionUID = 1L;
}
