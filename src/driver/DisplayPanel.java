package driver;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ConcurrentModificationException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import cobweb.UIInterface;
import cobweb.UIInterface.MouseMode;

/**
 * DisplayPanel is a Panel derivative useful for displaying a cobweb simulation. It uses an offscreen image to buffer
 * drawing, for flicker-free performance at the cost of memory and perhaps a little speed. Use of DisplayPanel is not
 * required in Cobweb, but it does automate display handling. Future enhancement: implement a ScrollingDisplayPanel for
 * large simulations.
 */
public class DisplayPanel extends JComponent implements ComponentListener {

	private final class MarkDonePainting implements Runnable {
		public void run() {
			donePainting = true;
		}
	}

	/**
	 * Mouse event listener for the simulation display panel
	 *
	 */
	private class Mouse extends MouseAdapter {

		int storedX = -1;

		int storedY = -1;

		long storedTick = -1;

		private void convertCoords(int x, int y) {
			int realX = -1;
			int realY = -1;
			long realTick = theUI.getCurrentTime();
			{
				if (       x >= borderLeft   && x < tileWidth  * mapWidth  + borderRight
						&& y >= borderHeight && y < tileHeight * mapHeight + borderHeight) {
					realX = (x - borderLeft) / tileWidth;
					realY = (y - borderHeight) / tileHeight;

					// Avoid multiple clicks on one spot
					if (storedX != realX || storedY != realY || storedTick != realTick) {
						onClick(realX, realY);
					}
					// Update
					storedX = realX;
					storedY = realY;
					storedTick = realTick;
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			convertCoords(e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (mode != MouseMode.Observe)
				convertCoords(e.getX(), e.getY());
		}
	} // Mouse

	private static final int THERMAL_MARKER_WIDTH = 16;

	private MouseMode mode = MouseMode.Observe;
	private int editType;

	private final Mouse myMouse = new Mouse();

	private static final int PADDING = 10;

	private int tileWidth = 0;

	private int tileHeight = 0;

	private int borderLeft = 0;

	private int borderHeight = 0;

	private int mapWidth = 0;

	private int mapHeight = 0;

	cobweb.UIInterface theUI;

	public static final long serialVersionUID = 0x09FE6158DCF2CA3BL;

	private boolean donePainting = true;

	private final Runnable markReadyRefresh = new MarkDonePainting();

	private int borderRight;

	public DisplayPanel(UIInterface ui) {
		theUI = ui;
		addComponentListener(this);
		addMouseListener(myMouse);
		addMouseMotionListener(myMouse);

	}

	private void onClick(int x, int y) {
		try {
			switch (mode) {
				case Observe:
					theUI.observe(x, y);
					break;

				case AddAgent:
					theUI.addAgent(x, y, editType);
					break;

				case AddFood:
					theUI.addFood(x, y, editType);
					break;

				case AddStone:
					theUI.addStone(x, y);
					break;
				case RemoveAgent:
					theUI.removeAgent(x, y);
					break;
				case RemoveFood:
					theUI.removeFood(x, y);
					break;
				case RemoveStone:
					theUI.removeStone(x, y);
					break;

			}
		} catch (ConcurrentModificationException ex) {
			// TODO LOW: Doesn't do anything bad, but a new agent might not show up if this exception occurs fix if possible
		}
	}

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

	/**
	 * Is the display grid done repainting since the last refresh(false) ?
	 *
	 * @return true when repaint is done
	 */
	public boolean isReadyToRefresh() {
		return donePainting;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		//TODO LOW : antialias?
//		Graphics2D g2 = (Graphics2D) g;
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.translate(borderLeft, borderHeight);
		theUI.draw(g, tileWidth, tileHeight);
		g.translate(-borderLeft, -borderHeight);
	}

	/**
	 * Refreshes the display grid
	 *
	 * @param wait wait for repaint to finish before returning?
	 */
	public void refresh(boolean wait) {
		if (wait) {
			donePainting = false;
			repaint();
			// Wait for displayPanel to repaint
			if (SwingUtilities.isEventDispatchThread()) {
				// When we are in the Swing thread, repaint() executes synchronously
				donePainting = true;
			} else {
				// Otherwise we need to wait for Swing thread to finish the repaint
				try {
					SwingUtilities.invokeAndWait(markReadyRefresh);
				} catch (InterruptedException ex) {
					donePainting = true;
				} catch (InvocationTargetException ex) {
					donePainting = true;
				}
			}
		} else if (donePainting) {
			// Start painting a new frame without waiting
			donePainting = false;
			repaint();
			SwingUtilities.invokeLater(markReadyRefresh);
		}
	}

	public void setMouseMode(MouseMode mode) {
		this.mode = mode;
	}

	public void setMouseMode(MouseMode mode, int type) {
		this.mode = mode;
		this.editType = type;
	}

	public void setUI(UIInterface ui) {
		theUI = ui;
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

		mapWidth = theUI.getWidth();
		mapHeight = theUI.getHeight();
		if (mapWidth != 0) {
			tileWidth = size.width / mapWidth;
		}
		if (mapHeight != 0) {
			tileHeight = size.height / mapHeight;
		}
		tileWidth = Math.min(tileWidth, tileHeight);
		tileHeight = tileWidth;
		int borderWidth = (size.width - tileWidth * theUI.getWidth() + PADDING) / 2;
		borderLeft = borderWidth + THERMAL_MARKER_WIDTH;
		borderRight = borderWidth - THERMAL_MARKER_WIDTH;
		borderHeight = (size.height - tileHeight * theUI.getHeight() + PADDING) / 2;

		this.refresh(false);
	}
}
