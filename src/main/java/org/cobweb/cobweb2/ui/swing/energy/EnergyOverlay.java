package org.cobweb.cobweb2.ui.swing.energy;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.ui.swing.DisplayOverlay;
import org.cobweb.cobweb2.ui.swing.OverlayGenerator;


public class EnergyOverlay implements OverlayGenerator {


	@Override
	public DisplayOverlay getDrawInfo(Simulation sim) {
		EnergyDrawInfo res = new EnergyDrawInfo(sim.theEnvironment.getPlugin(EnergyStats.class));
		return res;
	}
}
