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

import org.cobweb.cobweb2.plugins.stats.CauseTree.CauseTreeNode;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.ui.swing.config.Util;


public class EnergyEventConfig {

	public static JComponent createFilterConfigPanel(final EnergyStats energyStats) {

		final JTree tree = new JTree(new CauseTreeModel(energyStats.causeTree));

		tree.setCellRenderer(new CauseCellRenderer(energyStats, tree));

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

		JPanel buttons = new JPanel();
		buttons.add(whiteList);
		buttons.add(blackList);
		buttons.add(remove);


		JPanel result = new JPanel(new BorderLayout());
		result.add(treePane);
		result.add(buttons, BorderLayout.SOUTH);

		Util.makeGroupPanel(result, "Energy Cause Filter");
		return result;
	}


	private static abstract class TreeNodeAction extends AbstractAction {

		private final JTree tree;

		private TreeNodeAction(String name, JTree tree) {
			super(name);
			this.tree = tree;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;

			CauseTreeNode node = (CauseTreeNode) path.getLastPathComponent();
			action(node);
			((CauseTreeModel) tree.getModel()).fireNodeChanged(path);
		}
		protected abstract void action(CauseTreeNode node);

		private static final long serialVersionUID = 1L;
	}

	private static final class CauseCellRenderer extends DefaultTreeCellRenderer {

		private final EnergyStats energyStats;
		private final JTree tree;

		private CauseCellRenderer(EnergyStats energyStats, JTree tree) {
			this.energyStats = energyStats;
			this.tree = tree;
		}

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
	}

}
