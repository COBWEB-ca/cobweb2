package org.cobweb.cobweb2.ui.swing;

import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;


public interface DisplayOverlay {

	public void draw(java.awt.Graphics g, int tileWidth, int tileHeight, DisplaySettings settings);

}
