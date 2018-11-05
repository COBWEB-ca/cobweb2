package org.cobweb.cobweb2.plugins.fusion;

import org.cobweb.cobweb2.plugins.AgentState;
import org.cobweb.io.ConfXMLTag;

public class FusionState implements AgentState {

    @ConfXMLTag("AgentParams")
    public FusionAgentParams agentParams;

    @Deprecated
    public FusionState() {
    }

    public FusionState(FusionAgentParams params) {
        this.agentParams = params;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    private static final long serialVersionUID = 1L;
}
