package cobweb2test;

import junit.framework.TestCase;
import driver.CobwebApplicationRunner;

/**
 * Run common simulations to make sure they do not crash.
 * Only runs one simulation right now because CobwebApplicationRunner.main cannot be called twice.
 */
public class SimulationTest extends TestCase {

	/**
	 * Runs everything.xml
	 */
	public void testExperimentEverythingXml() {
		CobwebApplicationRunner.main("src/main/resources/experiments/everything.xml", "", true, 1000, false);
	}
}
