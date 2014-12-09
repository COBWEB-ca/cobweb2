package org.cobweb.swingutil;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;


public class JComponentWaiter implements SynchronousDisplay {

	private boolean donePaint = true;

	/**
	 * Runnable used to mark the control as refreshed
	 */
	private Runnable donePaintMarker = new Runnable() {
		@Override
		public void run() {
			donePaint = true;
		}
	};

	private final Component component;

	public JComponentWaiter(Component component) {
		this.component = component;

	}

	@Override
	public void refresh(boolean wait) {
		if (wait) {
			donePaint = false;
			component.repaint();
			// Wait for displayPanel to repaint
			if (SwingUtilities.isEventDispatchThread()) {
				// When we are in the Swing thread, repaint() executes synchronously
				donePaint = true;
			} else {
				// Otherwise we need to wait for Swing thread to finish the repaint
				try {
					SwingUtilities.invokeAndWait(donePaintMarker);
				} catch (InterruptedException ex) {
					donePaint = true;
				} catch (InvocationTargetException ex) {
					donePaint = true;
				}
			}
		} else if (donePaint) {
			// Start painting a new frame without waiting
			donePaint = false;
			component.repaint();
			SwingUtilities.invokeLater(donePaintMarker);
		}
	}

	@Override
	public boolean isReadyToRefresh() {
		return donePaint;
	}

}
