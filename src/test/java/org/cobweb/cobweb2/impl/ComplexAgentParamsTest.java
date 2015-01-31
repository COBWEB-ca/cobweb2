package org.cobweb.cobweb2.impl;

import org.cobweb.cobweb2.impl.ComplexAgentParams;
import org.cobweb.cobweb2.impl.ComplexEnvironmentParams;

import junit.framework.TestCase;

public class ComplexAgentParamsTest extends TestCase {

	public void testComplexAgentParams() {
		ComplexEnvironmentParams env = new ComplexEnvironmentParams();
		ComplexAgentParams p = new ComplexAgentParams(env);
		assertEquals(100, p.foodEnergy);
	}

}
