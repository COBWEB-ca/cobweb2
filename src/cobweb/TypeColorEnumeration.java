package cobweb;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class TypeColorEnumeration implements ColorLookup {

	private static ColorSpace space = ColorSpace.getInstance(ColorSpace.CS_sRGB);

	private static Color[] table = { Color.yellow, Color.cyan, Color.green, Color.red, Color.orange, Color.blue,
		Color.magenta, Color.pink };

	private static TypeColorEnumeration instance = new TypeColorEnumeration();

	public static TypeColorEnumeration getInstance() {
		return instance;
	}

	public java.awt.Color getColor(int index, int num) {

		Color c = table[index % table.length];
		while (index >= table.length) {
			index -= table.length;
			c = c.darker();
		}
		return c;

	}

	public ColorSpace getSpace() {
		return space;
	}

}
