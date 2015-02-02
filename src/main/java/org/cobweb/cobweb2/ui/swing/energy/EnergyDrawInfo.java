package org.cobweb.cobweb2.ui.swing.energy;

import java.awt.Color;
import java.awt.Graphics;

import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.ui.swing.OverlayUtils;
import org.cobweb.cobweb2.ui.swing.TileOverlay;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;
import org.cobweb.cobweb2.ui.swing.energy.EnergyEventViewer.EnergyStatsConfig;
import org.cobweb.swingutil.GradientUtil;
import org.cobweb.util.MathUtil;

public class EnergyDrawInfo extends TileOverlay {

	private float[][] map;
	private EnergyStatsConfig config;

	public EnergyDrawInfo(EnergyStats plugin, EnergyStatsConfig config) {
		this.config = config;
		map = plugin.map;
	}

	@Override
	public void draw(Graphics g, int tileWidth, int tileHeight, Topology topology, DisplaySettings settings) {
		OverlayUtils.fadeDisplay(g, tileWidth, tileHeight, topology, config.fade);
		super.draw(g, tileWidth, tileHeight, topology, settings);
	}

	@Override
	public void drawTile(Graphics g, int tileWidth, int tileHeight, int x, int y) {
		float scaledValue = map[x][y];
		float value = MathUtil.clamp(scaledValue / 2 + 0.5f, 0f, 1f);

		float size = Math.abs(scaledValue);

		int blockSize = (int) Math.sqrt(size * tileHeight * config.scale);

		Color c = GradientUtil.colorFromFloat(value, config.opacity);
		g.setColor(c);
		g.fillRect(
				tileWidth / 2 - blockSize /2,
				tileHeight / 2 - blockSize /2,
				blockSize, blockSize);
	}

}