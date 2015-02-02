package org.cobweb.cobweb2.ui.swing.production;

import java.awt.Color;
import java.awt.Graphics;

import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.production.ProductionMapper;
import org.cobweb.cobweb2.ui.swing.OverlayUtils;
import org.cobweb.cobweb2.ui.swing.TileOverlay;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;
import org.cobweb.util.ArrayUtilities;

public class Disp extends TileOverlay {

	private float[][] tiles;
	private float max;

	public Disp(ProductionMapper mapper) {
		tiles = ArrayUtilities.clone(mapper.getValues());
		max = mapper.getMax();
	}

	@Override
	public void draw(Graphics g, int tileWidth, int tileHeight, Topology topology, DisplaySettings settings) {
		OverlayUtils.fadeDisplay(g, tileWidth, tileHeight, topology, 0.9f);
		super.draw(g, tileWidth, tileHeight, topology, settings);
	}

	@Override
	public void drawTile(Graphics g, int tileWidth, int tileHeight, int x, int y) {

		int amount = 255 - (int) ((Math.min(tiles[x][y], max) / max) * 255f);
		Color c = new Color(amount / 2 + 127, amount, 255, (255 - amount) / 2);
		g.setColor(c);
		g.fillRect(0, 0, tileWidth, tileHeight);
	}


}