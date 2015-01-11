package org.cobweb.cobweb2.ui.swing;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.swingutil.WaitableJComponent;

/**
 * DisplayPanel is a Panel derivative useful for displaying a cobweb simulation. It uses an offscreen image to buffer
 * drawing, for flicker-free performance at the cost of memory and perhaps a little speed. Use of DisplayPanel is not
 * required in Cobweb, but it does automate display handling. Future enhancement: implement a ScrollingDisplayPanel for
 * large simulations.
 */
public class DisplayPanel extends WaitableJComponent implements ComponentListener {

	/**
	 * Mouse event listener for the simulation display panel
	 *
	 */
	private abstract class Mouse extends MouseAdapter {


		private Location convertCoords(int x, int y) {
			x -= borderLeft;
			y -= borderHeight;

			int realX = x / tileWidth;
			int realY = y / tileHeight;
			Location l = new Location(realX, realY);
			if (simulation.getTopology().isValidLocation(l))
				return l;
			else
				return null;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			dragMode = DragMode.Click;
			Location loc = convertCoords(e.getX(), e.getY());
			if (loc != null) {
				click(loc);
			}
		}



		@Override
		public void mouseReleased(MouseEvent e) {
			dragMode = DragMode.Click;
			super.mouseReleased(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (dragMode == DragMode.Click) {
				dragMode = DragMode.DragStart;
			}
			Location loc = convertCoords(e.getX(), e.getY());
			if (loc != null) {
				dragMode = drag(loc, dragMode);
			}
		}



		private void click(Location loc) {
			if(!canClick(loc))
				return;

			if (canSetOn(loc)) {
				setOn(loc);
			} else if (canSetOff(loc)) {
				setOff(loc);
			}
			refresh(false);
		}

		private DragMode drag(Location loc, DragMode dragmode) {
			if (!canClick(loc))
				return dragmode;
			if (dragmode == DragMode.DragStart) {
				if (canSetOn(loc)) {
					dragmode = DragMode.DragOn;
				} else if (canSetOff(loc)) {
					dragmode = DragMode.DragOff;
				}
			}

			if (dragmode == DragMode.DragOn && canSetOn(loc)) {
				setOn(loc);
			} else if (dragmode == DragMode.DragOff && canSetOff(loc)) {
				setOff(loc);
			}
			refresh(false);
			return dragmode;
		}
		abstract boolean canClick(Location loc);

		abstract boolean canSetOn(Location loc);
		abstract boolean canSetOff(Location loc);
		abstract void setOn(Location loc);
		abstract void setOff(Location loc);
	} // Mouse


	private class ObserveMouseListener extends Mouse {

		@Override
		public boolean canClick(Location loc) {
			return true;
		}

		@Override
		boolean canSetOn(Location loc) {
			return simulation.theEnvironment.getAgent(loc) != null && !canSetOff(loc);
		}

		@Override
		boolean canSetOff(Location loc) {
			return observedAgents.contains(simulation.theEnvironment.getAgent(loc));
		}

		@Override
		void setOn(Location loc) {
			ComplexAgent agent = (ComplexAgent)simulation.theEnvironment.getAgent(loc);
			if (agent != null)
				observedAgents.add(agent);
		}

		@Override
		void setOff(Location loc) {
			ComplexAgent agent = (ComplexAgent)simulation.theEnvironment.getAgent(loc);
			if (agent != null)
				observedAgents.remove(agent);
		}

	}

	private class StoneMouseListener extends Mouse {

		@Override
		public boolean canClick(Location loc) {
			return !simulation.theEnvironment.hasAgent(loc);
		}

		@Override
		boolean canSetOn(Location loc) {
			return !canSetOff(loc);
		}

		@Override
		boolean canSetOff(Location loc) {
			return simulation.theEnvironment.hasStone(loc);
		}

		@Override
		void setOn(Location loc) {
			simulation.theEnvironment.addStone(loc);
		}

		@Override
		void setOff(Location loc) {
			simulation.theEnvironment.removeStone(loc);
		}

	}

	private class AgentMouseListener extends Mouse {

		private int mytype;
		public AgentMouseListener(int type) {
			mytype = type;
		}

		@Override
		public boolean canClick(Location loc) {
			Agent a = simulation.theEnvironment.getAgent(loc);
			return (a == null && !simulation.theEnvironment.hasStone(loc)) ||
					(a != null && a.getType() == mytype);
		}

		@Override
		boolean canSetOn(Location loc) {
			Agent a = simulation.theEnvironment.getAgent(loc);
			return (a == null && !simulation.theEnvironment.hasStone(loc));
		}

		@Override
		boolean canSetOff(Location loc) {
			Agent a = simulation.theEnvironment.getAgent(loc);
			return (a != null && a.getType() == mytype);
		}

		@Override
		void setOn(Location loc) {
			simulation.theEnvironment.addAgent(loc, mytype);
		}

		@Override
		void setOff(Location loc) {
			simulation.theEnvironment.removeAgent(loc);
		}

	}

	private class FoodMouseListener extends Mouse {

		int mytype;

		public FoodMouseListener(int type) {
			mytype = type;
		}

		@Override
		public boolean canClick(Location loc) {
			return !simulation.theEnvironment.hasStone(loc);
		}

		@Override
		boolean canSetOn(Location loc) {
			return !simulation.theEnvironment.hasFood(loc) || simulation.theEnvironment.hasStone(loc);
		}

		@Override
		boolean canSetOff(Location loc) {
			return simulation.theEnvironment.hasFood(loc) && simulation.theEnvironment.getFood(loc) == mytype;
		}

		@Override
		void setOn(Location loc) {
			simulation.theEnvironment.addFood(loc, mytype);
		}

		@Override
		void setOff(Location loc) {
			simulation.theEnvironment.removeFood(loc);
		}

	}

	private static final int THERMAL_MARKER_WIDTH = 16;

	private Mouse myMouse;

	private static final int PADDING = 10;

	private int tileWidth = 0;

	private int tileHeight = 0;

	private int borderLeft = 0;

	private int borderHeight = 0;

	private int mapWidth = 0;

	private int mapHeight = 0;

	Simulation simulation;

	public static final long serialVersionUID = 0x09FE6158DCF2CA3BL;

	private DisplaySettings displaySettings;

	public DisplayPanel(Simulation simulation, DisplaySettings displaySettings) {
		this.simulation = simulation;
		this.displaySettings = displaySettings;
		addComponentListener(this);

		setMouse(new ObserveMouseListener());
	}

	private void setMouse(Mouse m) {
		removeMouseListener(myMouse);
		removeMouseMotionListener(myMouse);

		myMouse = m;

		addMouseListener(myMouse);
		addMouseMotionListener(myMouse);
	}

	enum DragMode {
		Click,
		DragStart,
		DragOn,
		DragOff
	}

	private DragMode dragMode = DragMode.Click;

	private DrawInfo drawInfo;

	private List<ComplexAgent> observedAgents = new ArrayList<ComplexAgent>();

	@Override
	public void componentHidden(ComponentEvent e) {
		// nothing
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// nothing
	}

	@Override
	public void componentResized(ComponentEvent e) {
		updateScale();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// nothing
	}

	@Override
	public void refresh(boolean wait) {
		synchronized(simulation.theEnvironment) {
			Iterator<ComplexAgent> ai = observedAgents.iterator();
			while (ai.hasNext()) {
				ComplexAgent a = ai.next();
				if (!a.isAlive())
					ai.remove();
			}
			drawInfo = new DrawInfo(simulation, observedAgents, displaySettings);
		}
		super.refresh(wait);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		//TODO LOW : antialias?
		//		Graphics2D g2 = (Graphics2D) g;
		//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.translate(borderLeft, borderHeight);

		drawInfo.draw(g, tileWidth, tileHeight);

		g.translate(-borderLeft, -borderHeight);
	}

	public void setMouseMode(MouseMode mode) {
		switch (mode) {
			case AddStone:
				setMouse(new StoneMouseListener());
				break;
			case Observe:
				setMouse(new ObserveMouseListener());
				break;
			default:
				break;
		}
	}

	public void setMouseMode(MouseMode mode, int type) {
		switch (mode) {
			case AddAgent:
				setMouse(new AgentMouseListener(type));
				break;
			case AddFood:
				setMouse(new FoodMouseListener(type));
				break;
			default:
				break;
		}
	}

	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
		updateScale();
	}

	void updateScale() {
		java.awt.Dimension size = getSize();
		if (size.width <= 0 || size.height <= 0) {
			return;
		}

		Insets ins = getInsets();
		size.width -= ins.left + ins.right + PADDING + THERMAL_MARKER_WIDTH;
		size.height -= ins.top + ins.bottom + PADDING;

		mapWidth = simulation.theEnvironment.topology.width;
		mapHeight = simulation.theEnvironment.topology.height;
		if (mapWidth != 0) {
			tileWidth = size.width / mapWidth;
		}
		if (mapHeight != 0) {
			tileHeight = size.height / mapHeight;
		}
		tileWidth = Math.min(tileWidth, tileHeight);
		tileHeight = tileWidth;
		int borderWidth = (size.width - tileWidth * simulation.theEnvironment.topology.width + PADDING) / 2;
		borderLeft = borderWidth + THERMAL_MARKER_WIDTH;
		borderHeight = (size.height - tileHeight * simulation.theEnvironment.topology.height + PADDING) / 2;

		repaint();
	}
}
