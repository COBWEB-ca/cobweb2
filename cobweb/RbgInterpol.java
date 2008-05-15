package cobweb;

import java.util.Random;
import java.awt.*;
import java.awt.color.*;

public class RbgInterpol implements ColorLookup {

	public static int rbgRange = 1 << 24;

	private static ColorSpace space = ColorSpace
			.getInstance(ColorSpace.CS_sRGB);

	public ColorSpace getSpace() {
		return space;
	}

	public java.awt.Color getColor(int index, int num) {
		return new Color(
				(int) ((double) rbgRange * ((double) index / (double) (num))));
	}

}
