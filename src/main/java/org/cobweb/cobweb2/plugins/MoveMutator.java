package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Agent;

public interface MoveMutator extends AgentMutator {

    /*
     * Override the default move. Returns true if it actually does (in case if there is a probability that
     * a move will be overridden). false otherwise.
     */
    boolean overrideMove(Agent agent);
}
