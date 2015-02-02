package org.cobweb.cobweb2.ui.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.abiotic.AbioticFactor;
import org.cobweb.cobweb2.plugins.abiotic.AbioticParams;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;


public class AbioticDrawInfo implements OverlayGenerator, DisplayOverlay {

	public List<FactorDrawInfo> factors = new ArrayList<>();

	public AbioticDrawInfo(AbioticParams params, Topology topology) {
		for (int i = 0; i < params.factors.size(); i++) {
			AbioticFactor f = params.factors.get(i);
			factors.add(new FactorDrawInfo(f, topology, i));
		}
	}



	public class FactorDrawInfo {

		float values[][];
		private Topology topology;
		private float max;
		private float min;
		private int index;

		public FactorDrawInfo(AbioticFactor f, Topology topology, int index) {
			this.topology = topology;
			this.index = index;
			values = new float[4][];


			max = f.getMax();
			min = f.getMin();

			values[0] = new float[topology.width];
			values[1] = new float[topology.width];

			float maxX = ((float)topology.width - 1) / topology.width;
			float maxY = ((float)topology.height - 1) / topology.height;

			for (int x = 0; x < topology.width; x++) {
				values[0][x] = normalize(f.getValue((float) x / topology.width, 0f));
				values[1][x] = normalize(f.getValue((float) x / topology.width, maxX));
			}

			values[2] = new float[topology.height];
			values[3] = new float[topology.height];
			for (int y = 0; y < topology.height; y++) {
				values[2][y] = normalize(f.getValue(0f, (float) y / topology.height));
				values[3][y] = normalize(f.getValue(maxY, (float) y / topology.height));
			}
		}

		private float normalize(float v) {
			return (v - min) / (max - min);
		}

		public void draw(Graphics g, int tileWidth, int tileHeight) {
			for (int y = 0; y < topology.height; y++) {
				g.setColor(colorFromFloat(values[2][y]));

				int x = -3 - index * 3;
				g.drawLine(x - 1, y * tileHeight, x, (y + 1) * tileHeight);
			}

			for (int y = 0; y < topology.height; y++) {
				g.setColor(colorFromFloat(values[3][y]));

				int x = tileWidth * topology.width +  3 + index * 3;
				g.drawLine(x - 1, y * tileHeight, x, (y + 1) * tileHeight);
			}

			for (int x = 0; x < topology.width; x++) {
				g.setColor(colorFromFloat(values[0][x]));

				int y = - 3 - index * 3;
				g.drawLine(x * tileWidth, y - 1, (x + 1) * tileHeight, y);
			}

			for (int x = 0; x < topology.width; x++) {
				g.setColor(colorFromFloat(values[1][x]));

				int y = tileHeight * topology.height +  3 + index * 3;
				g.drawLine(x * tileWidth, y - 1, (x + 1) * tileHeight, y);
			}
		}

	}

	private static Color colorFromFloat(float y) {
		return new Color(Color.HSBtoRGB((1 - y) * 2 / 3, 1f, 1f));
	}

	@Override
	public void draw(Graphics g, int tileWidth, int tileHeight, Topology topology, DisplaySettings settings) {
		for (FactorDrawInfo f : factors) {
			f.draw(g, tileWidth, tileHeight);
		}
	}

	@Override
	public DisplayOverlay getDrawInfo(Simulation sim) {
		return this;
	}

}
