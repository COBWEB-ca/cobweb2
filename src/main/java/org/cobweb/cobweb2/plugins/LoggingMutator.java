package org.cobweb.cobweb2.plugins;

import java.util.Collection;
import java.util.Collections;


public interface LoggingMutator extends AgentMutator {

	public Collection<String> logDataAgent(int agentType);

	public Collection<String> logDataTotal();

	public Collection<String> logHeadersAgent();

	public Collection<String> logHeaderTotal();

	public static final Collection<String> NO_DATA = Collections.emptyList();
}
