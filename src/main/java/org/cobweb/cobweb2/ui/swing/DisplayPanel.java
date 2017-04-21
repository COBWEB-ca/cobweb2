package org.cobweb.cobweb2.ui.swing;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.abiotic.AbioticMutator;
import org.cobweb.cobweb2.ui.swing.config.DisplaySettings;
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

		private DragMode dragMode = DragMode.Disable;

		@Override
		public void mouseClicked(MouseEvent e) {
			dragMode = DragMode.Disable;
			Location loc = convertCoords(e.getX(), e.getY());
			if (loc != null) {
				click(loc);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			dragMode = DragMode.Disable;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (dragMode == DragMode.Disable) {
				dragMode = DragMode.DragStart;
			}
			Location loc = convertCoords(e.getX(), e.getY());
			if (loc != null) {
				// Right mouse button = don't interpolate
				drag(loc, !SwingUtilities.isRightMouseButton(e));
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

		private Location dragStart = null;

		private void drag(Location loc, boolean interpolate) {
			if (!canClick(loc))
				return;
			if (dragMode == DragMode.DragStart) {
				if (canSetOn(loc)) {
					dragMode = DragMode.DragOn;
					dragStart = loc;
				} else if (canSetOff(loc)) {
					dragMode = DragMode.DragOff;
					dragStart = loc;
				} else {
					return;
				}
			}

			// Interpolate between previous drag location and current
			int dx = loc.x - dragStart.x;
			int dy = loc.y - dragStart.y;
			if (!interpolate || dx == 0 && dy == 0) {
				dragAcross(loc);
			}
			else if (Math.abs(dx) >= Math.abs(dy)) {
				// Drag vector longer in X, use it as base, calculate Y from it
				for (int x = 0; x != dx; x += Integer.signum(dx)) {
					int y = dy * x / dx;
					dragAcross(new Location(dragStart.x + x, dragStart.y + y));
				}
			} else {
				// Drag vector longer in Y, transposition of other case
				for (int y = 0; y != dy; y += Integer.signum(dy)) {
					int x = dx * y / dy;
					dragAcross(new Location(dragStart.x + x, dragStart.y + y));
				}
			}
			dragStart = loc;
			refresh(false);
		}

		private void dragAcross(Location loc) {
			if (dragMode == DragMode.DragOn && canSetOn(loc)) {
				setOn(loc);
			} else if (dragMode == DragMode.DragOff && canSetOff(loc)) {
				setOff(loc);
			}
		}

		abstract boolean canClick(Location loc);

		abstract boolean canSetOn(Location loc);
		abstract boolean canSetOff(Location loc);
		abstract void setOn(Location loc);
		abstract void setOff(Location loc);
	} // Mouse

	private enum DragMode {
		Disable,
		DragStart,
		DragOn,
		DragOff
	}

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
			return !simulation.theEnvironment.hasFood(loc) && !simulation.theEnvironment.hasStone(loc);
		}

		@Override
		boolean canSetOff(Location loc) {
			return simulation.theEnvironment.hasFood(loc) && simulation.theEnvironment.getFoodType(loc) == mytype;
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

	private Simulation simulation;

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

	private DrawInfo drawInfo;

	private List<ComplexAgent> observedAgents = new ArrayList<ComplexAgent>();

	private Map<Class<? extends OverlayGenerator>, OverlayGenerator> overlays = new LinkedHashMap<>();

	public void addOverlay(OverlayGenerator overlay) {
		overlays.put(overlay.getClass(), overlay);
	}

	public void removeOverlay(Class<? extends OverlayGenerator> type) {
		overlays.remove(type);
	}

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

			try {
				overlays.put(AbioticDrawInfo.class,
						new AbioticDrawInfo(
								simulation.theEnvironment.getPlugin(AbioticMutator.class).params,
								simulation.theEnvironment.topology)
						);
			} catch (NullPointerException ex) {
				// FIXME find out what crashes here sometimes
				// Crash is OK for now, updateScale gets invoked agian and fixes everything
			}
			drawInfo = new DrawInfo(simulation, observedAgents, displaySettings, overlays.values());
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

	public void simulationChanged(boolean continuation) {
		if (!continuation)
			this.observedAgents.clear();

		updateScale();
	}

	public void updateScale() {
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
