package org.cobweb.cobweb2.plugins.fusion;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.SimulationTimeSpace;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.ContactMutator;
import org.cobweb.cobweb2.plugins.StatefulMutatorBase;

public class FusionMutator extends StatefulMutatorBase<FusionState> implements ContactMutator {

    public FusionParams params;
    private SimulationTimeSpace sim;

    public FusionMutator() {
        super(FusionState.class);
    }

    public void setParams(SimulationTimeSpace sim, FusionParams params) {
        this.sim = sim;
        this.params = params;
    }

    @Override
    public void onContact(Agent bumper, Agent bumpee) {
        int tr = bumper.getType();
        int te = bumpee.getType();

        if (!(params.agentParams[tr].fusionEnabled && params.agentParams[te].fusionEnabled)) {
            return;
        }

        if ((params.agentParams[tr].sameType || params.agentParams[te].sameType) &&
                tr != te) {
            return;
        }

        if (params.agentParams[tr].probability > sim.getRandom().nextFloat() &&
                params.agentParams[te].probability > sim.getRandom().nextFloat()) {
            if (sim.getRandom().nextFloat() < 0.5) {
                int energy = (int) (bumper.getEnergy() * params.agentParams[te].efficiency);
                bumpee.changeEnergy(energy, new ComplexAgent.BumpAgentCause());
                bumper.die();
            } else {
                int energy = (int) (bumpee.getEnergy() * params.agentParams[tr].efficiency);
                bumper.changeEnergy(energy, new ComplexAgent.BumpAgentCause());
                bumpee.die();
            }
        }
    }

    @Override
    protected boolean validState(FusionState state) {
        return state != null;
    }
}
