package cobweb;

import java.awt.Color;
import java.awt.color.ColorSpace;

/**
 * ColorLookup is an interface for mapping numbers inside of finite ranges to
 * colors
 */
public interface ColorLookup {

	public Color getColor(int index, int num);

	public ColorSpace getSpace();
}
