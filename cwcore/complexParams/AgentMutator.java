package cwcore.complexParams;

import java.util.Collection;

public interface AgentMutator {

	public Collection<String> logHeadersAgent();

	public Collection<String> logDataAgent(int agentType);

	public Collection<String> logHeaderTotal();

	public Collection<String> logDataTotal();
}
