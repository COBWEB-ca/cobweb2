package org.cobweb.cobweb2.ui.swing;

import java.awt.Color;

import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.production.Product;
import org.cobweb.cobweb2.waste.Waste;
import org.cobweb.util.Point2D;

class DropDrawInfo {
	public Point2D pos;
	public Color col = Color.PINK;

	public DropDrawInfo(Point2D pos, Drop drop) {
		this.pos = pos;

		Color c = Color.PINK;

		if (drop instanceof Waste) {
			col = new Color(204, 102, 0);
		} else if (drop instanceof Product) {
			col = new Color(128, 0, 255);
		}
	}

}