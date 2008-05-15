package cobweb;

import java.util.Random;
import java.awt.*;
import java.awt.color.*;

/**
 * ColorLookup is an interface for mapping numbers inside of finite ranges to
 * colors
 */
public interface ColorLookup {
	public Color getColor(int index, int num);

	public ColorSpace getSpace();
}