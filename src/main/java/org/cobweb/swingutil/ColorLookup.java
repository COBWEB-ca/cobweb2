package org.cobweb.swingutil;

import java.awt.Color;

/**
 * ColorLookup is an interface for mapping numbers inside of finite ranges to
 * colors
 */
public interface ColorLookup {

	public Color getColor(int index, int num);
}
