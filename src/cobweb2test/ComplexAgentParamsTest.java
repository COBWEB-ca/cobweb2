package cobweb2test;

import junit.framework.TestCase;
import cwcore.complexParams.ComplexAgentParams;
import cwcore.complexParams.ComplexEnvironmentParams;

public class ComplexAgentParamsTest extends TestCase {

	public void testComplexAgentParams() {
		ComplexEnvironmentParams env = new ComplexEnvironmentParams();
		ComplexAgentParams p = new ComplexAgentParams(env);
		assertEquals(100, p.foodEnergy);
	}

}
