package org.cobweb.cobweb2.ui;

import java.io.Writer;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.ComplexAgentInfo;


public class AgentReporter {

	public static void report(Writer w, Simulation simulation) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w, false);

		ComplexAgentInfo.initStaticAgentInfo(simulation.getAgentTypeCount());

		ComplexAgentInfo.printAgentHeaders(pw);

		for (ComplexAgentInfo info : simulation.theEnvironment.agentInfoVector) {
			info.printInfo(pw);
		}
		pw.flush();
	}

}
