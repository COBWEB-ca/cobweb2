package org.cobweb.swingutil;

import java.awt.Color;


public class GradientUtil {

	public static Color colorFromFloat(float y) {
		return new Color(Color.HSBtoRGB((1 - y) * 2 / 3, 1f, 1f));
	}

	public static Color colorFromFloat(float y, byte alpha) {
		int intCol =
				Color.HSBtoRGB((1 - y) * 2 / 3, 1f, 1f) & 0xffffff |
				(alpha << 24);
		return new Color(intCol, true);
	}

}
