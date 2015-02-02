package org.cobweb.cobweb2.ui.swing;

import org.cobweb.cobweb2.ui.ViewerClosedCallback;
import org.cobweb.cobweb2.ui.ViewerPlugin;

public abstract class OverlayPluginViewer<T extends OverlayGenerator> implements ViewerPlugin {

	protected DisplayPanel panel;

	protected T overlay;

	protected ViewerClosedCallback onClosed;

	public OverlayPluginViewer(DisplayPanel panel) {
		this.panel = panel;
	}

	protected abstract T createOverlay();

	@Override
	public void on() {
		if (overlay != null)
			return;

		overlay = createOverlay();
		panel.addOverlay(overlay);
		panel.refresh(true);
	}

	@Override
	public void off() {
		if (overlay == null)
			return;

		panel.removeOverlay(overlay.getClass());
		overlay = null;
		panel.refresh(true);
	}

	@Override
	public void dispose() {
		off();
	}

	@Override
	public void setClosedCallback(ViewerClosedCallback onClosed) {
		this.onClosed = onClosed;
	}

}
