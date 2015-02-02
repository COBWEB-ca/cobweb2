package org.cobweb.cobweb2.ui.swing.energy;

import java.awt.Color;
import java.awt.Graphics;

import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.ui.swing.OverlayUtils;
import org.cobweb.cobweb2.ui.swing.TileOverlay;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;
import org.cobweb.swingutil.GradientUtil;
import org.cobweb.util.MathUtil;

public class EnergyDrawInfo extends TileOverlay {

	private float[][] map;
	private float absMax;

	public EnergyDrawInfo(EnergyStats plugin) {
		map = plugin.map;
		absMax = Math.max(plugin.max, -plugin.min) / 10;
	}

	@Override
	public void draw(Graphics g, int tileWidth, int tileHeight, Topology topology, DisplaySettings settings) {
		OverlayUtils.fadeDisplay(g, tileWidth, tileHeight, topology);
		super.draw(g, tileWidth, tileHeight, topology, settings);
	}

	@Override
	public void drawTile(Graphics g, int tileWidth, int tileHeight, int x, int y) {
		float scaledValue = map[x][y] / absMax;
		float value = MathUtil.clamp(scaledValue / 2 + 0.5f, 0f, 1f);

		float size = Math.abs(scaledValue);

		int blockSize = (int) Math.sqrt(size * tileHeight * 10);

		Color c = GradientUtil.colorFromFloat(value, (byte)128);
		g.setColor(c);
		g.fillRect(
				tileWidth / 2 - blockSize /2,
				tileHeight / 2 - blockSize /2,
				blockSize, blockSize);
	}

}