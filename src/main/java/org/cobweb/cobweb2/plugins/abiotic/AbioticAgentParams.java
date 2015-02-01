package org.cobweb.cobweb2.plugins.abiotic;

import java.util.Arrays;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

/**
 * Contains abiotic parameters for agent type.
 */
public class AbioticAgentParams implements ParameterSerializable {

	@ConfDisplayName("Factor")
	@ConfXMLTag("FactorParams")
	@ConfList(indexName = "Factor", startAtOne = true)
	public AgentFactorParams[] factorParams = new AgentFactorParams[0];

	public void resizeFields(int fieldCount) {
		AgentFactorParams[] n = Arrays.copyOf(factorParams, fieldCount);
		for (int i = factorParams.length; i < n.length; i++) {
			n[i] = new AgentFactorParams();
		}
		factorParams = n;
	}

	private static final long serialVersionUID = 2L;
}
