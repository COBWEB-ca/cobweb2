package cobweb;



public interface ViewerPlugin {

	public String getName();
	public void on();
	public void off();
	public void setClosedCallback(ViewerClosedCallback onClosed);
}
