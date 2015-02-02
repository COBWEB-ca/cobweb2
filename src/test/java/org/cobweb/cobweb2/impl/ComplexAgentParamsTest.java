package org.cobweb.cobweb2.impl;

import junit.framework.TestCase;

import org.cobweb.cobweb2.SimulationConfig;

public class ComplexAgentParamsTest extends TestCase {

	public void testComplexAgentParams() {
		SimulationConfig sim = new SimulationConfig();
		assertEquals(100, sim.agentParams.agentParams[0].foodEnergy);
	}

}
