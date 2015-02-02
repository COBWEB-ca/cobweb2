package org.cobweb.cobweb2.ui.swing.energy;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.plugins.stats.CauseTree.CauseTreeNode;
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
	private Simulation simulation;

	public EnergyEventViewer(DisplayPanel panel, Simulation sim) {
		super(panel);
		this.simulation = sim;
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
			filterDialog = createConfigFrame(simulation.theEnvironment.getPlugin(EnergyStats.class));
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

	private JFrame createConfigFrame(EnergyStats energyStats) {
		JFrame result = new JFrame("Energy Overlay Configuration");
		Container mainPanel = result.getContentPane();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));


		JComponent sp = createDisplayConfigPanel();
		sp.setPreferredSize(new Dimension(200,200));
		mainPanel.add(sp);

		JComponent filter = createFilterConfigPanel(energyStats);
		mainPanel.add(filter);

		result.pack();

		return result;
	}

	private JComponent createDisplayConfigPanel() {
		ConfigTableModel model = new ConfigTableModel(new ParameterSerializable[] { energyStatsConfig }, "Value");

		MixedValueJTable paramTable = new MixedValueJTable(model);
		JScrollPane sp = new JScrollPane(paramTable);
		Util.makeGroupPanel(sp, "Display Options");
		return sp;
	}

	private static JComponent createFilterConfigPanel(final EnergyStats energyStats) {

		JTree tree = new JTree(new CauseTreeModel(energyStats));

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {

				Component res = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				setText(((CauseTreeNode)value).getName());
				return res;
			}
			private static final long serialVersionUID = 1L;
		});

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		JScrollPane result = new JScrollPane(tree);
		Util.makeGroupPanel(result, "Energy Change Causes");

		return result;
	}

}
