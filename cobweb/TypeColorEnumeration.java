package cobweb;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class TypeColorEnumeration implements ColorLookup {

	private static ColorSpace space = ColorSpace
			.getInstance(ColorSpace.CS_sRGB);

	private static Color[] table = { Color.yellow, Color.cyan, Color.green,
			Color.red, Color.orange, Color.blue, Color.magenta };

	public ColorSpace getSpace() {
		return space;
	}

	public java.awt.Color getColor(int index, int num) {
		if (index < table.length)
			return table[index];
		else
			return Color.black;
	}

	private static TypeColorEnumeration instance = new TypeColorEnumeration();

	public static TypeColorEnumeration getInstance() {
		return instance;
	}

}
