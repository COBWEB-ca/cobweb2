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

	@Override
	public AbioticAgentParams clone() {
		try {
			AbioticAgentParams copy = (AbioticAgentParams) super.clone();
			copy.factorParams = new AgentFactorParams[this.factorParams.length];

			for (int i = 0; i < copy.factorParams.length; i++) {
				copy.factorParams[i] = this.factorParams[i].clone();
			}
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 2L;
}
