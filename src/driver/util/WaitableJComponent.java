package driver.util;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * JComponent that can be repainted synchronously
 */
public class WaitableJComponent extends JComponent implements SynchronousDisplay {

	public WaitableJComponent() {
		super();
	}

	private static final long serialVersionUID = 3027479023284219300L;

	private boolean donePaint = true;

	/**
	 * Runnable used to mark the control as refreshed
	 */
	private Runnable donePaintMarker = new Runnable() {
		public void run() {
			donePaint = true;
		}
	};

	public void refresh(boolean wait) {
		if (wait) {
			donePaint = false;
			repaint();
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
			repaint();
			SwingUtilities.invokeLater(donePaintMarker);
		}
	}

	public boolean isReadyToRefresh() {
		return donePaint;
	}
}