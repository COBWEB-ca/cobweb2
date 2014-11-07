package cwcore.complexParams;

import java.util.Collection;

/**
 * Modifies agents' parameters during the simulation.
 */
public interface AgentMutator {

	public Collection<String> logDataAgent(int agentType);

	public Collection<String> logDataTotal();

	public Collection<String> logHeadersAgent();

	public Collection<String> logHeaderTotal();
}
