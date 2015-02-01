package org.cobweb.cobweb2.ui.swing.energy;

import java.awt.Color;
import java.awt.Graphics;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.ui.swing.DisplayOverlay;
import org.cobweb.cobweb2.ui.swing.OverlayGenerator;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;
import org.cobweb.swingutil.GradientUtil;
import org.cobweb.util.MathUtil;


public class EnergyOverlay implements OverlayGenerator {


	public static class EnergyDrawInfo implements DisplayOverlay {

		private float[][] map;
		private float min;
		private float max;

		public EnergyDrawInfo(EnergyStats plugin) {
			map = plugin.map;
			max = plugin.max;
			min = plugin.min;
		}

		@Override
		public void draw(Graphics g, int tileWidth, int tileHeight, DisplaySettings settings) {
			g.setColor(new Color(1f,1f,1f,0.8f));
			g.fillRect(0,0, map.length * tileWidth, map[0].length * tileHeight);

			float absMax = Math.max(max, -min) / 10;
			for (int x = 0; x < map.length; x++) {
				for (int y = 0; y < map[x].length; y++) {
					float scaledValue = map[x][y] / absMax;
					float value = MathUtil.clamp(scaledValue / 2 + 0.5f, 0f, 1f);

					float size = Math.abs(scaledValue);

					int blockSize = (int) Math.sqrt(size * tileHeight * 10);

					Color c = GradientUtil.colorFromFloat(value, (byte)128);
					g.setColor(c);
					g.fillRect(
							x * tileWidth + tileWidth / 2 - blockSize /2,
							y * tileHeight + tileHeight / 2 - blockSize /2,
							blockSize, blockSize);
				}
			}
		}

	}

	public EnergyOverlay() {
	}


	@Override
	public DisplayOverlay getDrawInfo(Simulation sim) {
		EnergyDrawInfo res = new EnergyDrawInfo(sim.theEnvironment.getPlugin(EnergyStats.class));
		return res;
	}
}
