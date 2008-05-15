package cobweb;

import java.util.Random;
import java.awt.*;
import java.awt.color.*;

public class GAColorEnumeration implements ColorLookup {

	private static ColorSpace space = ColorSpace
			.getInstance(ColorSpace.CS_sRGB);

	private static Color[] table = { Color.red, Color.green, Color.blue,
			Color.yellow, Color.orange, Color.magenta, Color.cyan };

	public ColorSpace getSpace() {
		return space;
	}

	public java.awt.Color getColor(int index, int num) {
		if (index < table.length)
			return table[index];
		else
			return Color.black;
	}

}