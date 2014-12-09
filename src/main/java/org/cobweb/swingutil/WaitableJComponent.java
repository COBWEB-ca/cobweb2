package org.cobweb.swingutil;

import javax.swing.JComponent;

/**
 * JComponent that can be repainted synchronously
 */
public class WaitableJComponent extends JComponent implements SynchronousDisplay {

	JComponentWaiter waiter = new JComponentWaiter(this);

	@Override
	public void refresh(boolean wait) {
		waiter.refresh(wait);
	}

	@Override
	public boolean isReadyToRefresh() {
		return waiter.isReadyToRefresh();
	}

	public WaitableJComponent() {
		super();
	}

	private static final long serialVersionUID = 3027479023284219300L;


}