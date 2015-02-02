package org.cobweb.cobweb2.ui.swing.energy;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.ui.swing.DisplayOverlay;
import org.cobweb.cobweb2.ui.swing.DisplayPanel;
import org.cobweb.cobweb2.ui.swing.OverlayGenerator;
import org.cobweb.cobweb2.ui.swing.OverlayPluginViewer;


public class EnergyEventViewer extends OverlayPluginViewer<EnergyEventViewer> implements OverlayGenerator {


	public EnergyEventViewer(DisplayPanel panel) {
		super(panel);
	}

	@Override
	public String getName() {
		return "Energy Changes";
	}

	@Override
	protected EnergyEventViewer createOverlay() {
		return this;
	}


	@Override
	public DisplayOverlay getDrawInfo(Simulation sim) {
		EnergyDrawInfo res = new EnergyDrawInfo(sim.theEnvironment.getPlugin(EnergyStats.class));
		return res;
	}
}
