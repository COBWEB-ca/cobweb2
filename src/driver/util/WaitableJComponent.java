package driver.util;

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
		if (wait && !donePaint)
			return;

		donePaint = false;
		repaint();

		if (wait) {
			if (!SwingUtilities.isEventDispatchThread())
				try {
					SwingUtilities.invokeAndWait(donePaintMarker);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}

			else
				donePaint = true;

		} else {

			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeLater(donePaintMarker);
		}
	}

	public boolean isReadyToRefresh() {
		return donePaint;
	}
}