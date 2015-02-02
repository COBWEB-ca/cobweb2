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
import org.cobweb.cobweb2.ui.swing.stats.RegionViewer.RegionViewerOptions;


public class RegionOverlay implements DisplayOverlay {

	private GridStats stats;
	private RegionViewerOptions viewerOptions;

	public RegionOverlay(GridStats stats, RegionViewerOptions viewerOptions) {
		this.stats = stats;
		this.viewerOptions = viewerOptions;

	}

	@Override
	public void draw(Graphics g, int tileWidth, int tileHeight, Topology topology, DisplaySettings settings) {
		OverlayUtils.fadeDisplay(g, tileWidth, tileHeight, topology, viewerOptions.fade);

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

		int leftColW = (int) g.getFontMetrics().getStringBounds((stats.types + 1) + "   ", g).getWidth();
		float numColW = (wantWidth - leftColW) / 2;
		int rightColStart = (int) (leftColW + numColW);

		for (CellStats[] cols : stats.cellStats) {
			for (CellStats cell : cols) {
				g.translate(cell.xb * tileWidth, cell.yb * tileHeight);

				g.setColor(Color.BLACK);
				g.drawRect(0, 0, cell.w * tileWidth, cell.h * tileHeight);

				for (int i = 0; i < stats.types; i++) {
					int y = ascent + i * lineHeight;

					if (viewerOptions.graphs) {
						g.setColor(settings.agentColor.getColor(i, stats.types));
						if (cell.totalAgents() > 0)
							g.fillRect(leftColW, y - lineHeight / 2, (int)(numColW * 0.8 * cell.agentCount[i] / cell.totalAgents()) , lineHeight / 2);
						if (cell.totalFood() > 0)
							g.fillRect(rightColStart, y - lineHeight / 2, (int)(numColW * 0.8 * cell.foodCount[i] / cell.totalFood()), lineHeight / 2);
					}

					g.setColor(Color.BLACK);
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
