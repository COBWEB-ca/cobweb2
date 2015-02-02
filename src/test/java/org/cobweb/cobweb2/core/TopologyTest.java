package org.cobweb.cobweb2.core;

import junit.framework.TestCase;

import org.cobweb.util.RandomNoGenerator;


public class TopologyTest extends TestCase {

	private static final Location l00 = new Location(0, 0);
	private static final Location l01 = new Location(0, 1);
	private static final Location l09 = new Location(0, 9);
	private static final Location l10 = new Location(1, 0);
	private static final Location l11 = new Location(1, 1);
	private static final Location l34 = new Location(3, 4);
	private static final Location l49 = new Location(4, 9);
	private static final Location l50 = new Location(5, 0);
	private static final Location l65 = new Location(9-3, 9-4);
	private static final Location l89 = new Location(8, 9);
	private static final Location l90 = new Location(9, 0);
	private static final Location l98 = new Location(9, 8);
	private static final Location l99 = new Location(9, 9);

	// 00 10 __ __ __ 50 __ __ __ 90
	// 01 11 __ __ __ __ __ __ __ __
	// __ __ __ __ __ __ __ __ __ __
	// __ __ __ __ __ __ __ __ __ __
	// __ __ __ 34 __ __ __ __ __ __
	// __ __ __ __ __ __ 65 __ __ __
	// __ __ __ __ __ __ __ __ __ __
	// __ __ __ __ __ __ __ __ __ __
	// __ __ __ __ __ __ __ __ __ 98
	// 09 __ __ __ 49 __ __ __ 89 99


	private static class TestRandomSource implements RandomSource {
		RandomNoGenerator random = new RandomNoGenerator();

		@Override
		public RandomNoGenerator getRandom() {
			return random;
		}
	}

	private RandomSource randomSource = new TestRandomSource();

	public void testDistanceSimple() {
		Topology t = new Topology(randomSource, 10, 10, false);

		assertEquals(0.0, t.getDistance(l00, l00));
		assertEquals(1.0, t.getDistance(l00, l01));
		assertEquals(1.0, t.getDistance(l00, l10));
		assertEquals(Math.sqrt(2.0), t.getDistance(l00, l11));
		assertEquals(5.0, t.getDistance(l00, l34));

		assertEquals(9.0, t.getDistance(l00, l09));
		assertEquals(9.0, t.getDistance(l00, l90));
	}

	public void testDistanceWrap() {
		Topology t = new Topology(randomSource, 10, 10, true);

		assertEquals(0.0, t.getDistance(l00, l00));
		assertEquals(1.0, t.getDistance(l00, l01));
		assertEquals(1.0, t.getDistance(l00, l10));
		assertEquals(5.0, t.getDistance(l00, l34));

		assertEquals(9.0, t.getDistance(l00, l09));
		assertEquals(1.0, t.getDistance(l00, l90)); // Horizontal wrap
		assertEquals(1.0, t.getDistance(l00, l50)); // Vertical wrap

		assertEquals(0.0, t.getDistance(l99, l99));
		assertEquals(1.0, t.getDistance(l99, l98));
		assertEquals(1.0, t.getDistance(l99, l89));
		assertEquals(5.0, t.getDistance(l99, l65));

		assertEquals(9.0, t.getDistance(l99, l90));
		assertEquals(1.0, t.getDistance(l99, l09)); // Horizontal wrap
		assertEquals(1.0, t.getDistance(l99, l49)); // Vertical wrap
	}



}
