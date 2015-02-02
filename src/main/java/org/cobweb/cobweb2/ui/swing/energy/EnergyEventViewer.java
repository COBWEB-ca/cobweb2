package org.cobweb.cobweb2.ui.swing.energy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

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
		@ConfDisplayName("Marker energy scale")
		public float scale = 1f;

		@ConfDisplayName("Marker minimum size")
		public int minSize = 5;

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

		final CauseTreeModel treeModel = new CauseTreeModel(energyStats.causeTree);
		final JTree tree = new JTree(treeModel);

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree t, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean focus) {

				Component res = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				CauseTreeNode node = (CauseTreeNode)value;
				setText(node.getName());

				if (energyStats.whiteList.contains(node.type)) {
					setForeground(Color.GREEN);
				} else if (energyStats.blackList.contains(node.type)) {
					setForeground(Color.RED);
				} else if (!energyStats.isWatching(node.type)) {
					setForeground(Color.LIGHT_GRAY);
				}
				return res;
			}
			private static final long serialVersionUID = 1L;
		});

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		JScrollPane treePane = new JScrollPane(tree);
		Util.makeGroupPanel(treePane, "Energy Change Causes");

		JButton whiteList = new JButton(new AbstractAction("Whitelist") {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionPath();
				if (path == null)
					return;

				CauseTreeNode node = (CauseTreeNode) path.getLastPathComponent();
				energyStats.whitelist(node.type);
				treeModel.fireNodeChanged(path);
			}
			private static final long serialVersionUID = 1L;
		});

		JButton blackList = new JButton(new AbstractAction("Blacklist") {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionPath();
				if (path == null)
					return;

				CauseTreeNode node = (CauseTreeNode) path.getLastPathComponent();
				energyStats.blacklist(node.type);
				treeModel.fireNodeChanged(path);
			}
			private static final long serialVersionUID = 1L;
		});

		JButton remove = new JButton(new AbstractAction("Unlist") {
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath path = tree.getSelectionPath();
				if (path == null)
					return;

				CauseTreeNode node = (CauseTreeNode) path.getLastPathComponent();
				energyStats.unlist(node.type);
				treeModel.fireNodeChanged(path);
			}
			private static final long serialVersionUID = 1L;
		});

		JPanel buttons = new JPanel();
		buttons.add(whiteList);
		buttons.add(blackList);
		buttons.add(remove);


		JPanel result = new JPanel(new BorderLayout());
		result.add(treePane);
		result.add(buttons, BorderLayout.SOUTH);
		return result;
	}

}
