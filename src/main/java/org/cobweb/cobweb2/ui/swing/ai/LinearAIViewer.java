package org.cobweb.cobweb2.ui.swing.ai;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.cobweb.cobweb2.impl.ai.LinearWeightsControllerParams;
import org.cobweb.cobweb2.ui.ViewerClosedCallback;
import org.cobweb.cobweb2.ui.ViewerPlugin;


public class LinearAIViewer implements ViewerPlugin {

	private LinearAIGraph aiGraph;
	private ViewerClosedCallback onClosed;
	private LinearWeightsControllerParams controllerParams;

	public LinearAIViewer(LinearWeightsControllerParams controllerParams) {
		this.controllerParams = controllerParams;

	}

	@Override
	public void on() {
		aiGraph = new LinearAIGraph(controllerParams);
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
		off();
	}
}