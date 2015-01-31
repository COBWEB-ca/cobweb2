package org.cobweb.cobweb2;

import junit.framework.TestCase;

import org.cobweb.cobweb2.ui.swing.CobwebApplicationRunner;

/**
 * Run common simulations to make sure they do not crash.
 * Only runs one simulation right now because CobwebApplicationRunner.main cannot be called twice.
 */
public class SimulationTest extends TestCase {

	/**
	 * Runs everything.xml
	 */
	public void testExperimentEverythingXml() {
		CobwebApplicationRunner.main("src/main/resources/experiments/everything.xml", "testlog.tsv", true, 2000, false);
	}
}
