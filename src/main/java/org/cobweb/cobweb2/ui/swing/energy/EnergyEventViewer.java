package org.cobweb.cobweb2.ui.swing.energy;

import org.cobweb.cobweb2.ui.swing.DisplayPanel;
import org.cobweb.cobweb2.ui.swing.OverlayPluginViewer;


public class EnergyEventViewer extends OverlayPluginViewer<EnergyOverlay> {


	public EnergyEventViewer(DisplayPanel panel) {
		super(panel);
	}

	@Override
	public String getName() {
		return "Energy Changes";
	}

	@Override
	protected EnergyOverlay createOverlay() {
		return new EnergyOverlay();
	}
}
