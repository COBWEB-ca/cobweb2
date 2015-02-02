package org.cobweb.swingutil;

import java.awt.Color;

public class TypeColorEnumeration implements ColorLookup {

	private static Color[] table = { Color.yellow, Color.cyan, Color.green, Color.red, Color.orange, Color.blue,
		Color.magenta, Color.pink };

	@Override
	public java.awt.Color getColor(int index, int num) {
		// generates any number of colors, num bound is ignored.
		Color c = table[index % table.length];
		while (index >= table.length) {
			index -= table.length;
			c = c.darker();
		}
		return c;

	}
}
