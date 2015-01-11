package org.cobweb.cobweb2.ui.swing;

import org.cobweb.swingutil.ColorLookup;
import org.cobweb.swingutil.TypeColorEnumeration;

/**
 * Defines how the simulation is displayed, agent colors, etc.
 */
public class DisplaySettings {

	/**
	 * Maps agent type number to a color
	 */
	public ColorLookup agentColor = new TypeColorEnumeration();

}
