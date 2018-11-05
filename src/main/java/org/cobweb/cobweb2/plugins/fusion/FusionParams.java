package org.cobweb.cobweb2.plugins.fusion;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;

public class FusionParams extends PerAgentParams<FusionAgentParams> {

    private AgentFoodCountable size;

    public FusionParams(AgentFoodCountable size) {
        super(FusionAgentParams.class, size);
    }

    @Override
    protected FusionAgentParams newAgentParam() {
        return new FusionAgentParams();
    }

}
