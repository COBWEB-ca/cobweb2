package org.cobweb.cobweb2.ui.swing.energy;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Map;
import java.util.Map.Entry;

import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.plugins.stats.EnergyStats.LocationStats;
import org.cobweb.cobweb2.ui.swing.DisplayOverlay;
import org.cobweb.cobweb2.ui.swing.OverlayUtils;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;
import org.cobweb.cobweb2.ui.swing.energy.EnergyEventViewer.EnergyStatsConfig;
import org.cobweb.swingutil.GradientUtil;
import org.cobweb.util.MathUtil;

public class EnergyDrawInfo implements DisplayOverlay {

	private Map<Location, LocationStats> locationStats;
	private EnergyStatsConfig config;

	public EnergyDrawInfo(EnergyStats plugin, EnergyStatsConfig config) {
		this.config = config;
		locationStats = plugin.locationStats;
	}

	@Override
	public void draw(Graphics g, int tileWidth, int tileHeight, Topology topology, DisplaySettings settings) {
		OverlayUtils.fadeDisplay(g, tileWidth, tileHeight, topology, config.fade);
		for (Entry<Location, LocationStats> ls : locationStats.entrySet()) {
			Location l = ls.getKey();

			g.translate(l.x * tileWidth, l.y * tileHeight);
			drawTile(g, tileWidth, tileHeight, ls.getValue());
			g.translate(-l.x * tileWidth, -l.y * tileHeight);
		}
	}

	public void drawTile(Graphics g, int tileWidth, int tileHeight, LocationStats stats ) {
		float scaledValue = stats.total;
		float value = MathUtil.clamp(scaledValue / 2 + 0.5f, 0f, 1f);

		float size = Math.abs(scaledValue);

		int blockSize = (int) Math.sqrt(size * tileHeight * config.scale) + 4;

		Color c = GradientUtil.colorFromFloat(value, config.opacity);
		g.setColor(c);
		g.fillRect(
				tileWidth / 2 - blockSize /2,
				tileHeight / 2 - blockSize /2,
				blockSize, blockSize);
	}

}