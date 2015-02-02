package org.cobweb.cobweb2.ui.swing.stats;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.ui.GridStats;
import org.cobweb.cobweb2.ui.GridStats.RegionOptions;
import org.cobweb.cobweb2.ui.swing.DisplayOverlay;
import org.cobweb.cobweb2.ui.swing.DisplayPanel;
import org.cobweb.cobweb2.ui.swing.OverlayGenerator;
import org.cobweb.cobweb2.ui.swing.OverlayPluginViewer;


public class RegionViewer extends OverlayPluginViewer<RegionViewer> implements OverlayGenerator {

	private RegionOptions regionOptions = new RegionOptions();

	public RegionViewer(DisplayPanel panel) {
		super(panel);
	}

	@Override
	public String getName() {
		return "Regional Stats";
	}

	@Override
	public DisplayOverlay getDrawInfo(Simulation sim) {
		return new RegionOverlay(new GridStats(sim, regionOptions));
	}

	@Override
	protected RegionViewer createOverlay() {
		return this;
	}

}
