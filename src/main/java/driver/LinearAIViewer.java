package driver;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import cobweb.ViewerClosedCallback;
import cobweb.ViewerPlugin;


public class LinearAIViewer implements ViewerPlugin {

	private LinearAIGraph aiGraph;
	private ViewerClosedCallback onClosed;

	@Override
	public void on() {
		aiGraph = new LinearAIGraph();
		aiGraph.setVisible(true);
		aiGraph.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClosed.viewerClosed();
			}
		});
	}

	@Override
	public void off() {
		if (aiGraph == null)
			return;
		aiGraph.setVisible(false);
		aiGraph.setEnabled(false);
		aiGraph = null;
	}

	@Override
	public String getName() {
		return "AI Weight Stats";
	}

	@Override
	public void setClosedCallback(ViewerClosedCallback onClosed) {
		this.onClosed = onClosed;

	}

	@Override
	public void dispose() {
		// nothing
	}
}