package org.cobweb.cobweb2.ui.swing;

import java.awt.Graphics;

import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;

public abstract class TileOverlay implements DisplayOverlay {

	@Override
	public void draw(Graphics g, int tileWidth, int tileHeight, Topology topology, DisplaySettings settings) {
		for (int x = 0; x < topology.width; x++) {
			for (int y = 0; y < topology.height; y++) {
				g.translate(x * tileWidth, y * tileHeight);
				drawTile(g, tileWidth, tileHeight, x, y);
				g.translate(-x * tileWidth, -y * tileHeight);
			}
		}
	}

	/**
	 * Draw tile for coordinate (x,y). The origin of graphics during the call is the top left corner of the tile
	 */
	public abstract void drawTile(Graphics g, int tileWidth, int tileHeight, int x, int y);

}
