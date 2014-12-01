package org.cobweb.cobweb2.ui.swing.production;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Scheduler;
import org.cobweb.cobweb2.production.ProductionMapper;
import org.cobweb.cobweb2.ui.ViewerClosedCallback;
import org.cobweb.cobweb2.ui.ViewerPlugin;

public class ProductionViewer implements ViewerPlugin {

	private Disp productionDisplay;
	private ViewerClosedCallback onClosed;

	private ComplexEnvironment theEnvironment;
	private Scheduler theScheduler;


	public ProductionViewer(Environment theEnvironment, Scheduler theScheduler) {
		super();
		this.theEnvironment = (ComplexEnvironment) theEnvironment;
		this.theScheduler = theScheduler;
	}

	@Override
	public void on() {

		if (productionDisplay != null) {
			theScheduler.removeSchedulerClient(productionDisplay);
			productionDisplay.setVisible(false);
			productionDisplay.setEnabled(false);
			productionDisplay.dispose();
		}

		ProductionMapper newMapper = (theEnvironment).prodMapper;
		productionDisplay = new Disp(newMapper, theEnvironment.getWidth(), theEnvironment.getHeight());

		productionDisplay.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClosed.viewerClosed();
			}
		});

		theScheduler.addSchedulerClient(productionDisplay);
	}

	@Override
	public void off() {
		if (productionDisplay != null) {
			theScheduler.removeSchedulerClient(productionDisplay);
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