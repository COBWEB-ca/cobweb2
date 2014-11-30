package org.cobweb.cobweb2.test.ga;

import org.cobweb.cobweb2.genetics.GeneticCode;

import junit.framework.TestCase;

public class GeneticCodeTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		gc = new GeneticCode(4);

	}

	GeneticCode gc;


	public void test1() {
		String in = "00000001"; // 1
		gc.bitsFromString(0, 8, in, 0);
		assertEquals(in, gc.stringFromBits(0, 8));
		assertEquals(1  , gc.getValue(0));
	}

	public void test2() {
		String in2 = "xx11110000xxxxxxxx"; // 240
		gc.bitsFromString(8, 8, in2, 2);
		assertEquals("11110000", gc.stringFromBits(8, 8));
		assertEquals(240, gc.getValue(1));
	}

	public void test3() {

		String in3 = "10110111"; // 183
		gc.bitsFromString(16, 8, in3, 0);
		assertEquals(183, gc.getValue(2));
	}

	public void test4() {
		String in = "10110111" + "11110000" + "00000001"; // 1
		gc.bitsFromString(0, 24, in, 0);

		assertEquals(in, gc.stringFromBits(0, 24));

	}

	public void test5() {
		gc.setValue(0, 0);
		gc.setValue(1, 30);
		gc.setValue(2, 90);
		gc.setValue(3, 180);

		assertEquals("10110100" + "01011010" + "00011110" + "00000000", gc.stringFromBits(0, 32));

		assertEquals(0.0f, gc.getStatus(0), 0.000000000000001f);
		assertEquals(1.0f, gc.getStatus(1), 0.000000000000001f);
		assertEquals(2.0f, gc.getStatus(2), 0.000000000000001f);
		assertEquals(0.0f, gc.getStatus(3), 0.000000000000001f);
	}

}
