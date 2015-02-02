package org.cobweb.cobweb2.ui;




public interface ViewerPlugin {

	public String getName();
	public void on();
	public void off();
	public void dispose();
	public void setClosedCallback(ViewerClosedCallback onClosed);
}
