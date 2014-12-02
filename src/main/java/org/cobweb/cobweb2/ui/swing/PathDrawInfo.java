package org.cobweb.cobweb2.ui.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cobweb.cobweb2.core.Location;

class PathDrawInfo {

	private final List<Location> path;

	public PathDrawInfo(List<Location> path) {
		this.path = new LinkedList<Location>(path);
	}

	public void draw(Graphics g, int tileWidth, int tileHeight) {
		Iterator<Location> itr = path.iterator();
		if (!itr.hasNext()) {
			return;
		}
		Location p1;
		Location p2 = itr.next();
		Color o = g.getColor();

		int alpha = 63;

		int increment = 192 / path.size();

		while (itr.hasNext()) {
			g.setColor(new Color(0, 0, 255, alpha));
			p1 = p2;
			p2 = itr.next();
			int x = Math.min(p1.v[0], p2.v[0]);
			int y = Math.min(p1.v[1], p2.v[1]);
			int w = Math.abs(p1.v[0] - p2.v[0]);
			int h = Math.abs(p1.v[1] - p2.v[1]);

			if (w > 1 || h > 1) {
				continue;
			}

			int stripwidth = (tileWidth - tileWidth * 4 / 5) + 1;
			int stripheight = (tileHeight - tileHeight * 4 / 5) + 1;

			x = x * tileWidth + tileWidth * 2 / 5;
			y = y * tileHeight + tileHeight * 2 / 5;
			w = w * tileWidth + stripwidth;
			h = h * tileHeight + stripheight;

			if (p1.v[0] - p2.v[0] > 0) {
				w -= stripwidth;
				x += stripwidth;
			} else if (p1.v[0] - p2.v[0] < 0) {
				w -= stripwidth;
			} else if (p1.v[1] - p2.v[1] > 0) {
				h -= stripheight;
				y += stripheight;
			} else if (p1.v[1] - p2.v[1] < 0) {
				h -= stripheight;
			}

			g.fillRect(x, y, w, h);
			alpha += increment;
		}
		g.setColor(o);
	}
}