package org.cobweb.cobweb2.ui.swing.energy;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.ui.swing.DisplayOverlay;
import org.cobweb.cobweb2.ui.swing.DisplayPanel;
import org.cobweb.cobweb2.ui.swing.OverlayGenerator;
import org.cobweb.cobweb2.ui.swing.OverlayPluginViewer;
import org.cobweb.cobweb2.ui.swing.config.ConfigTableModel;
import org.cobweb.cobweb2.ui.swing.config.MixedValueJTable;
import org.cobweb.cobweb2.ui.swing.config.Util;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ParameterSerializable;


public class EnergyEventViewer extends OverlayPluginViewer<EnergyEventViewer> implements OverlayGenerator {


	private JFrame filterDialog;

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
		EnergyDrawInfo res = new EnergyDrawInfo(sim.theEnvironment.getPlugin(EnergyStats.class), energyStatsConfig);
		return res;
	}

	@Override
	public void on() {
		super.on();

		if (filterDialog == null) {
			filterDialog = createConfigFrame();
			filterDialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					off();
					onClosed.viewerClosed();
				}
			});
			filterDialog.setVisible(true);
		}
	}

	@Override
	public void off() {
		super.off();

		if (filterDialog != null) {
			filterDialog.dispose();
			filterDialog = null;
		}
	}

	private EnergyStatsConfig energyStatsConfig = new EnergyStatsConfig();

	public static class EnergyStatsConfig implements ParameterSerializable {
		@ConfDisplayName("Scale")
		public float scale = 1f;

		@ConfDisplayName("Fade background")
		public float fade = 0.8f;

		@ConfDisplayName("Overlay opacity")
		public float opacity = 0.5f;

		private static final long serialVersionUID = 1L;
	}

	private JFrame createConfigFrame() {
		JFrame result = new JFrame("Energy Overlay Configuration");
		Container mainPanel = result.getContentPane();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));


		ConfigTableModel model = new ConfigTableModel(new ParameterSerializable[] {energyStatsConfig }, "Value");

		MixedValueJTable paramTable = new MixedValueJTable(model);
		JScrollPane sp = new JScrollPane(paramTable);
		Util.makeGroupPanel(sp, "Display Options");

		mainPanel.add(sp);


		result.pack();

		return result;
	}
}
