package org.cobweb.cobweb2.plugins.production;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;


public class ProductionState implements AgentState {

	@ConfXMLTag("AgentParams")
	public ProductionAgentParams agentParams;


	@Deprecated // for reflection use only!
	public ProductionState() {
	}

	public ProductionState(ProductionAgentParams agentParams) {
		this.agentParams = agentParams;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	protected ProductionState clone() {
		try {
			return (ProductionState) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
