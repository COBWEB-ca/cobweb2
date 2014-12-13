package org.cobweb.cobweb2.ui.swing.production;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.production.ProductionMapper;
import org.cobweb.cobweb2.ui.SimulationRunner;
import org.cobweb.cobweb2.ui.ViewerClosedCallback;
import org.cobweb.cobweb2.ui.ViewerPlugin;

public class ProductionViewer implements ViewerPlugin {

	private Disp productionDisplay;
	private ViewerClosedCallback onClosed;

	private SimulationRunner theScheduler;


	public ProductionViewer(SimulationRunner theScheduler) {
		super();
		this.theScheduler = theScheduler;
	}

	@Override
	public void on() {

		if (productionDisplay != null) {
			theScheduler.removeUIComponent(productionDisplay);
			productionDisplay.setVisible(false);
			productionDisplay.setEnabled(false);
			productionDisplay.dispose();
		}

		ProductionMapper newMapper = ((Simulation)theScheduler.getSimulation()).prodMapper;
		productionDisplay = new Disp(newMapper);

		productionDisplay.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClosed.viewerClosed();
			}
		});

		theScheduler.addUIComponent(productionDisplay);
	}

	@Override
	public void off() {
		if (productionDisplay != null) {
			theScheduler.removeUIComponent(productionDisplay);
			productionDisplay.setVisible(false);
			productionDisplay.setEnabled(false);
			productionDisplay.dispose();
		}
		productionDisplay = null;
	}

	@Override
	public String getName() {
		return "Production map";
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