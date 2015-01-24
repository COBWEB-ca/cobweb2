package org.cobweb.cobweb2.ui.swing;

import java.awt.Color;
import java.awt.Graphics;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.genetics.GeneticCode;
import org.cobweb.swingutil.ColorLookup;

/**
 * AgentDrawInfo stores the drawable state of a single agent. AgentDrawInfo
 * exists to make the data passed to newAgent calls persist for subsequent
 * draw calls. Note that this class is private to LocalUIInterface; no other
 * class (other than LocalUIInterface.DrawInfo) knows of the existence of
 * this class.
 */
class AgentDrawInfo {

	/** Solid color of the agent. */
	private final java.awt.Color agentColor;

	private final java.awt.Color type;

	private final java.awt.Color action;

	/** Position in tile coordinates */
	private final LocationDirection position;

	AgentDrawInfo(ComplexAgent agent, ColorLookup colorMap, Simulation sim) {
		int[] rgb = new int[3];
		if (sim.geneticMutator != null) {
			GeneticCode genes = sim.geneticMutator.getGene(agent);
			for (int i = 0; i < Math.min(3, genes.getNumGenes()); i++) {
				rgb[i] = genes.getValue(i);
			}
		}

		if (sim.diseaseMutator != null) {
			if (sim.diseaseMutator.isSick(agent))
				rgb[2] = 255;
		}

		agentColor = new Color(rgb[0], rgb[1], rgb[2]);

		type =  colorMap.getColor(agent.params.type, sim.getAgentTypeCount());

		position = agent.getPosition();

		action = agent.getAgentPDActionCheat() ? Color.RED : Color.BLACK;
	}

	void draw(Graphics g, int tileWidth, int tileHeight) {
		g.setColor(agentColor);
		int topLeftX = position.x * tileWidth;
		int topLeftY = position.y * tileHeight;

		if (position.direction.x != 0 || position.direction.y != 0) {
			int deltaX = tileWidth / 2;
			int deltaY = tileHeight / 2;
			int centerX = topLeftX + deltaX;
			int centerY = topLeftY + deltaY;


			int[] xPts = new int[3];
			int[] yPts = new int[3];

			if (position.direction.x != 0 && position.direction.y != 0) {
				// Diagonal; deal with this later
			} else if (position.direction.x != 0) {
				// Horizontal facing...
				xPts[0] = centerX + position.direction.x * deltaX;
				xPts[1] = centerX - position.direction.x * deltaX;
				xPts[2] = xPts[1];

				yPts[0] = centerY;
				yPts[1] = centerY + deltaY;
				yPts[2] = centerY - deltaY;
			} else {
				// Vertical facing...
				xPts[0] = centerX;
				xPts[1] = centerX + deltaX;
				xPts[2] = centerX - deltaX;

				yPts[0] = centerY + position.direction.y * deltaY;
				yPts[1] = centerY - position.direction.y * deltaY;
				yPts[2] = yPts[1];
			}
			g.fillPolygon(xPts, yPts, 3);
			g.setColor(type);
			g.fillOval(topLeftX + tileWidth / 3, topLeftY + tileHeight / 3, tileWidth / 3 + 1, tileHeight / 3 + 1);
			g.setColor(action);
			g.drawPolygon(xPts, yPts, 3);
		} else {
			g.fillOval(topLeftX, topLeftY, tileWidth, tileHeight);
		}
	}
}