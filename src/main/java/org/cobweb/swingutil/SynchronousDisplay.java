package org.cobweb.swingutil;

/**
 * A display that can be refreshed synchronously
 */
public interface SynchronousDisplay {

	/**
	 * Checks if the display is ready to be refreshed
	 * @return true when display is ready to be refreshed
	 */
	public boolean isReadyToRefresh();

	/**
	 * Refreshes the display
	 * @param synch refresh synchronously?
	 */
	public void refresh(boolean synch);

}