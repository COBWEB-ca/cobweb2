package driver;

import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * DisplayPanel is a Panel derivative useful for displaying a cobweb simulation.
 * It uses an offscreen image to buffer drawing, for flicker-free performance at
 * the cost of memory and perhaps a little speed. Use of DisplayPanel is not
 * required in Cobweb, but it does automate display handling. Future
 * enhancement: implement a ScrollingDisplayPanel for large simulations.
 */
public class DisplayPanel extends JComponent implements ComponentListener {

	public DisplayPanel(cobweb.UIInterface ui) {
		theUI = ui;
		addComponentListener(this);
	}

	public void setUI(cobweb.UIInterface ui) {
		theUI = ui;
		updateScale();
	}

	@Override
	public void paintComponent(java.awt.Graphics g) {
		super.paintComponent(g);
		g.translate(borderWidth, borderHeight);
		theUI.draw(g, tileWidth, tileHeight);
		g.translate(-borderWidth, -borderHeight);
	}

	private static final int PADDING = 4;

	void updateScale() {
		java.awt.Dimension size = getSize();
		if (size.width <= 0 || size.height <= 0) {
			return;
		}

		Insets ins = getInsets();
		size.width -= ins.left + ins.right + PADDING;
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
		borderWidth = (size.width - tileWidth * theUI.getWidth() + PADDING) / 2;
		borderHeight = (size.height - tileHeight * theUI.getHeight() + PADDING) / 2;

		this.refresh(false);
	}

	/*
	 * the following information needed to convert the mouse coords into the
	 * grid coordinates
	 */
	public int getWidthInTiles() {
		return theUI.getWidth();
	}

	public int getHeightInTiles() {
		return theUI.getHeight();
	}

	public int getTileW() {
		return tileWidth;
	}

	public int getTileH() {
		return tileHeight;
	}

	public int getBorderHeight() {
		return borderHeight;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	private int tileWidth = 0;

	private int tileHeight = 0;

	private int borderWidth = 0;

	private int borderHeight = 0;

	private int mapWidth = 0;

	private int mapHeight = 0;

	cobweb.UIInterface theUI;

	public static final long serialVersionUID = 0x09FE6158DCF2CA3BL;

	private boolean donePainting = true;
	
	private final Runnable markReadyRefresh = new Runnable() {
		public void run() {
			donePainting = true;
		}
	};
	
	/**
	 * Refreshes the display grid
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
		}
		else if (donePainting) {
			// Start painting a new frame without waiting
			donePainting = false;
			repaint();
			SwingUtilities.invokeLater(markReadyRefresh);
		}
	}
	
	/**
	 * Is the display grid done repainting since the last refresh(false) ?
	 * @return true when repaint is done
	 */
	public boolean isReadyToRefresh() {
		return donePainting;
	}

	public void componentResized(ComponentEvent e) {
		updateScale();
	}

	public void componentHidden(ComponentEvent e) {
		// nothing
	}

	public void componentShown(ComponentEvent e) {
		// nothing
	}

	public void componentMoved(ComponentEvent e) {
		// nothing
	}
}
