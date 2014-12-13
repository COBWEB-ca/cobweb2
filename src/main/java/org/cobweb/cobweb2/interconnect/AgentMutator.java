package org.cobweb.cobweb2.interconnect;

import java.util.Collection;
import java.util.Collections;

/**
 * Modifies agents' parameters during the simulation.
 */
public interface AgentMutator {

	public Collection<String> logDataAgent(int agentType);

	public Collection<String> logDataTotal();

	public Collection<String> logHeadersAgent();

	public Collection<String> logHeaderTotal();

	public static final Collection<String> NO_DATA = Collections.emptyList();
}
