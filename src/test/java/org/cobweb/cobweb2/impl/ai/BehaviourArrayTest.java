package org.cobweb.cobweb2.impl.ai;

import junit.framework.TestCase;

import org.junit.Test;


public class BehaviourArrayTest extends TestCase {

	@Test
	public void testOneOutput() {
		BehaviorArray ba = new BehaviorArray(8, new int[]{ 8 });

		int input = 2;
		int output = 254;
		ba.set(input, output);
		assertEquals(output, ba.getOutput(input)[0]);

	}

	public void testMoreOutputs() {

		BehaviorArray ba = new BehaviorArray(3, new int[]{ 1, 2});
		int input = 2;
		int output = (1 << 0) | (2 << 1);

		ba.set(input, output);
		assertEquals(output, ba.get(input));

		int[] outputArray = ba.getOutput(input);
		assertEquals(1, outputArray[0]);
		assertEquals(2, outputArray[1]);
	}

}
