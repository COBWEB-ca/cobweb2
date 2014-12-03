package org.cobweb.cobweb2.ui.swing;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Agent;
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


		private boolean convertCoords(int x, int y, int[] out) {
			x -= borderLeft;
			y -= borderHeight;
			if (!(     x >= 0 && x < tileWidth  * mapWidth 
					&& y >= 0 && y < tileHeight * mapHeight)) {
				return false;
			}

			int realX = x / tileWidth;
			int realY = y / tileHeight;

			out[0] = realX;
			out[1] = realY;
			return true;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			dragMode = DragMode.Click;
			int[] out = { 0, 0 };
			if (convertCoords(e.getX(), e.getY(), out)) {
				click(out[0], out[1]);
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
			int[] out = {0,0};
			if (convertCoords(e.getX(), e.getY(), out)) {
				dragMode = drag(out[0], out[1], dragMode);
			}
		}



		private void click(int x, int y) {
			if(!canClick(x, y))
				return;

			if (canSetOn(x, y)) {
				setOn(x, y);
			} else if (canSetOff(x, y)) {
				setOff(x, y);
			}
		}

		private DragMode drag(int x, int y, DragMode dragmode) {
			if (!canClick(x, y))
				return dragmode;
			if (dragmode == DragMode.DragStart) {
				if (canSetOn(x, y)) {
					dragmode = DragMode.DragOn;
				} else if (canSetOff(x, y)) {
					dragmode = DragMode.DragOff;
				}
			} 

			if (dragmode == DragMode.DragOn && canSetOn(x, y)) {
				setOn(x, y);
			} else if (dragmode == DragMode.DragOff && canSetOff(x, y)) {
				setOff(x, y);
			}
			return dragmode;
		}
		abstract boolean canClick(int x, int y);

		abstract boolean canSetOn(int x, int y);
		abstract boolean canSetOff(int x, int y);
		abstract void setOn(int x, int y);
		abstract void setOff(int x, int y);
	} // Mouse


	private class ObserveMouseListener extends Mouse {

		@Override
		public boolean canClick(int x, int y) {
			return true;
		}

		@Override
		boolean canSetOn(int x, int y) {
			return simulation.theEnvironment.getAgent(x, y) != null;
		}

		@Override
		boolean canSetOff(int x, int y) {
			return !canSetOn(x, y);
		}

		@Override
		void setOn(int x, int y) {
			// FIXME simulation.theEnvironment.observe(x, y);
		}

		@Override
		void setOff(int x, int y) {
			// FIXME simulation.theEnvironment.unObserve();
		}

	}

	private class StoneMouseListener extends Mouse {

		@Override
		public boolean canClick(int x, int y) {
			return !simulation.theEnvironment.hasAgent(x, y);
		}

		@Override
		boolean canSetOn(int x, int y) {
			return !canSetOff(x, y);
		}

		@Override
		boolean canSetOff(int x, int y) {
			return simulation.theEnvironment.hasStone(x, y);
		}

		@Override
		void setOn(int x, int y) {
			simulation.theEnvironment.addStone(x, y);
		}

		@Override
		void setOff(int x, int y) {
			simulation.theEnvironment.removeStone(x, y);
		}

	}

	private class AgentMouseListener extends Mouse {

		private int mytype;
		public AgentMouseListener(int type) {
			mytype = type;
		}

		@Override
		public boolean canClick(int x, int y) {
			Agent a = simulation.theEnvironment.getAgent(x, y);
			return (a == null && !simulation.theEnvironment.hasStone(x, y)) || 
					(a != null && a.type() == mytype);
		}

		@Override
		boolean canSetOn(int x, int y) {
			Agent a = simulation.theEnvironment.getAgent(x, y);
			return (a == null && !simulation.theEnvironment.hasStone(x, y));
		}

		@Override
		boolean canSetOff(int x, int y) {
			Agent a = simulation.theEnvironment.getAgent(x, y);
			return (a != null && a.type() == mytype);
		}

		@Override
		void setOn(int x, int y) {
			simulation.theEnvironment.addAgent(x, y, mytype);
		}

		@Override
		void setOff(int x, int y) {
			simulation.theEnvironment.removeAgent(x, y);
		}

	}

	private class FoodMouseListener extends Mouse {

		int mytype;

		public FoodMouseListener(int type) {
			mytype = type;
		}

		@Override
		public boolean canClick(int x, int y) {
			return !simulation.theEnvironment.hasStone(x, y);
		}

		@Override
		boolean canSetOn(int x, int y) {
			return !simulation.theEnvironment.hasFood(x, y) || simulation.theEnvironment.hasStone(x, y);
		}

		@Override
		boolean canSetOff(int x, int y) {
			return simulation.theEnvironment.hasFood(x, y) && simulation.theEnvironment.getFood(x, y) == mytype;
		}

		@Override
		void setOn(int x, int y) {
			simulation.theEnvironment.addFood(x, y, mytype);
		}

		@Override
		void setOff(int x, int y) {
			simulation.theEnvironment.removeFood(x, y);
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

	public DisplayPanel(Simulation simulation) {
		this.simulation = simulation;
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

	public void componentHidden(ComponentEvent e) {
		// nothing
	}

	public void componentMoved(ComponentEvent e) {
		// nothing
	}

	public void componentResized(ComponentEvent e) {
		updateScale();
	}

	public void componentShown(ComponentEvent e) {
		// nothing
	}

	@Override
	public void refresh(boolean wait) {
		drawInfo = new DrawInfo(simulation.theEnvironment);
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

		mapWidth = simulation.theEnvironment.getWidth();
		mapHeight = simulation.theEnvironment.getHeight();
		if (mapWidth != 0) {
			tileWidth = size.width / mapWidth;
		}
		if (mapHeight != 0) {
			tileHeight = size.height / mapHeight;
		}
		tileWidth = Math.min(tileWidth, tileHeight);
		tileHeight = tileWidth;
		int borderWidth = (size.width - tileWidth * simulation.theEnvironment.getWidth() + PADDING) / 2;
		borderLeft = borderWidth + THERMAL_MARKER_WIDTH;
		borderHeight = (size.height - tileHeight * simulation.theEnvironment.getHeight() + PADDING) / 2;

		this.refresh(false);
	}
}
