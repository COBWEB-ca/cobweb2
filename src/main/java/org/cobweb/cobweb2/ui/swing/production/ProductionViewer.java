package org.cobweb.cobweb2.ui.swing.production;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.plugins.production.ProductionMapper;
import org.cobweb.cobweb2.ui.swing.DisplayOverlay;
import org.cobweb.cobweb2.ui.swing.DisplayPanel;
import org.cobweb.cobweb2.ui.swing.OverlayGenerator;
import org.cobweb.cobweb2.ui.swing.OverlayPluginViewer;

public class ProductionViewer extends OverlayPluginViewer<ProductionViewer> implements OverlayGenerator {


	public ProductionViewer(DisplayPanel panel) {
		super(panel);
	}

	@Override
	public String getName() {
		return "Production Value";
	}

	@Override
	public DisplayOverlay getDrawInfo(Simulation sim) {
		return new Disp(sim.theEnvironment.getPlugin(ProductionMapper.class));
	}

	@Override
	protected ProductionViewer createOverlay() {
		return this;
	}


}