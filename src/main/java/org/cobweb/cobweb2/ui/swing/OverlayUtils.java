package org.cobweb.cobweb2.ui.swing;

import java.awt.Color;
import java.awt.Graphics;

import org.cobweb.cobweb2.core.Topology;


public class OverlayUtils {

	public static void fadeDisplay(Graphics g, int tileWidth, int tileHeight, Topology topology, float amount) {
		g.setColor(new Color(1f, 1f, 1f, amount));
		g.fillRect(0,0, topology.width * tileWidth, topology.height * tileHeight);
	}

}
