package org.cobweb.cobweb2.ui.swing.stats;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.ui.GridStats;
import org.cobweb.cobweb2.ui.GridStats.CellStats;
import org.cobweb.cobweb2.ui.swing.DisplayOverlay;
import org.cobweb.cobweb2.ui.swing.OverlayUtils;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;


public class RegionOverlay implements DisplayOverlay {

	private GridStats stats;

	public RegionOverlay(GridStats stats) {
		this.stats = stats;

	}

	@Override
	public void draw(Graphics g, int tileWidth, int tileHeight, Topology topology, DisplaySettings settings) {
		OverlayUtils.fadeDisplay(g, tileWidth, tileHeight, topology, 0.75f);

		Font originalFont = g.getFont();

		float haveHeight = g.getFontMetrics().getHeight();
		float wantHeight = (float) tileHeight * topology.height / stats.opts.vDivisions / (stats.types + 2.2f);
		float hScale = wantHeight / haveHeight;

		float haveWidth = (float) g.getFontMetrics().getStringBounds("22 A:20 F:20", g).getWidth();
		float wantWidth = tileWidth * topology.width / stats.opts.vDivisions;
		float vScale = wantWidth / haveWidth;

		float wantScale = Math.min(vScale, hScale);

		g.setFont(originalFont.deriveFont(originalFont.getSize2D() * wantScale));
		int lineHeight = g.getFontMetrics().getHeight();
		int ascent = g.getFontMetrics().getAscent();

		int leftColW = (int) g.getFontMetrics().getStringBounds((stats.types + 1) + "  ", g).getWidth();
		int rightColStart = (int) (leftColW + (wantWidth - leftColW) / 2);

		for (CellStats[] cols : stats.cellStats) {
			for (CellStats cell : cols) {
				g.translate(cell.xb * tileWidth, cell.yb * tileHeight);

				g.setColor(Color.BLACK);
				g.drawRect(0, 0, cell.w * tileWidth, cell.h * tileHeight);

				for (int i = 0; i < stats.types; i++) {
					int y = ascent + i * lineHeight;
					g.drawString((i + 1) + " ", 4, y);
					g.drawString("A:" + cell.agentCount[i], leftColW, y);
					g.drawString("F:" + cell.foodCount[i], rightColStart, y);
				}
				{
					int y = ascent + stats.types * lineHeight;
					g.drawString("T ", 4, y);
					g.drawString("A:" + cell.totalAgents(), leftColW, y);
					g.drawString("F:" + cell.totalFood(), rightColStart, y);

					g.drawString("Area: " + cell.area(), 4, ascent + (stats.types + 1) * lineHeight);
				}

				g.translate(-cell.xb * tileWidth, -cell.yb * tileHeight);
			}
		}

		g.setFont(originalFont);
	}

}
