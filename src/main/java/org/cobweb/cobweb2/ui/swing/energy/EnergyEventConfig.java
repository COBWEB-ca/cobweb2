package org.cobweb.cobweb2.ui.swing.energy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.cobweb.cobweb2.plugins.stats.CauseTree.CauseTreeNode;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.ui.swing.config.Util;
import org.jdesktop.swingx.JXTreeTable;


public class EnergyEventConfig {

	public static JComponent createFilterConfigPanel(final EnergyStats energyStats) {

		final JXTreeTable tree = new JXTreeTable(new CauseStatsTreeTableModel(energyStats.causeTree, energyStats));
		tree.getColumn(0).setPreferredWidth(200);
		tree.getColumn(1).setPreferredWidth(100);
		tree.getColumn(2).setPreferredWidth(100);
		tree.setRootVisible(true);
		tree.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.setTreeCellRenderer(new CauseCellRenderer(energyStats));

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		JScrollPane treePane = new JScrollPane(tree);

		JButton whiteList = new JButton(new TreeNodeAction("Whitelist", tree) {
			@Override
			protected void action(CauseTreeNode node) {
				energyStats.whitelist(node.type);
			}
			private static final long serialVersionUID = 1L;
		});

		JButton blackList = new JButton(new TreeNodeAction("Blacklist", tree) {
			@Override
			protected void action(CauseTreeNode node) {
				energyStats.blacklist(node.type);
			}
			private static final long serialVersionUID = 1L;
		});

		JButton remove = new JButton(new TreeNodeAction("Unlist", tree) {
			@Override
			protected void action(CauseTreeNode node) {
				energyStats.unlist(node.type);
			}
			private static final long serialVersionUID = 1L;
		});

		JButton clearStats = new JButton(new AbstractAction("Clear Stats") {
			@Override
			public void actionPerformed(ActionEvent e) {
				energyStats.resetStats();
				tree.repaint();
			}
			private static final long serialVersionUID = 1L;
		});

		JPanel buttons = new JPanel();
		buttons.add(whiteList);
		buttons.add(blackList);
		buttons.add(remove);
		buttons.add(clearStats);


		JPanel result = new JPanel(new BorderLayout());
		result.add(treePane);
		result.add(buttons, BorderLayout.SOUTH);

		Util.makeGroupPanel(result, "Energy Event Filter");
		return result;
	}


	private static abstract class TreeNodeAction extends AbstractAction {

		private final JXTreeTable tree;

		private TreeNodeAction(String name, JXTreeTable tree) {
			super(name);
			this.tree = tree;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getPathForRow(tree.getSelectedRow());
			if (path == null)
				return;

			CauseTreeNode node = (CauseTreeNode) path.getLastPathComponent();
			action(node);
			((CauseStatsTreeTableModel) tree.getTreeTableModel()).fireNodeChanged(path);
			tree.repaint();
		}
		protected abstract void action(CauseTreeNode node);

		private static final long serialVersionUID = 1L;
	}

	private static final class CauseCellRenderer extends DefaultTreeCellRenderer {

		private final EnergyStats energyStats;

		private CauseCellRenderer(EnergyStats energyStats) {
			this.energyStats = energyStats;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree t, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean focus) {

			Component res = super.getTreeCellRendererComponent(t, value, sel, expanded, leaf, row, hasFocus);
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
	}

}
