package org.cobweb.cobweb2.ui.swing.energy;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.ui.ViewerClosedCallback;
import org.cobweb.cobweb2.ui.ViewerPlugin;
import org.cobweb.cobweb2.ui.swing.DisplayPanel;


public class EnergyEventViewer implements ViewerPlugin {

	private DisplayPanel panel;
	private Simulation sim;
	private EnergyOverlay overlay;

	public EnergyEventViewer(Simulation sim, DisplayPanel panel) {
		this.sim = sim;
		this.panel = panel;
	}

	@Override
	public String getName() {
		return "Energy Changes";
	}

	@Override
	public void on() {
		if (overlay != null)
			return;

		overlay = new EnergyOverlay();
		panel.addOverlay(overlay);
	}

	@Override
	public void off() {
		if (overlay == null)
			return;

		panel.removeOverlay(EnergyOverlay.class);
		overlay = null;
	}

	@Override
	public void dispose() {
		off();
	}

	@Override
	public void setClosedCallback(ViewerClosedCallback onClosed) {
		// TODO Auto-generated method stub

	}


}
