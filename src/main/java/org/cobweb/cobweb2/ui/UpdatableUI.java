package org.cobweb.cobweb2.ui;


/**
 * UI component that can be updated
 */
public interface UpdatableUI {

	/**
	 * Updates UI.
	 * @param synchronous whether to wait for component to complete update before returning
	 */
	public void update(boolean synchronous);

	/**
	 * Checks if the UI is free to update right now.
	 * @return true when ready
	 */
	public boolean isReadyToRefresh();

}
