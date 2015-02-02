package org.cobweb.cobweb2.ui.swing.energy;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.cobweb.cobweb2.plugins.stats.CauseTree.CauseTreeNode;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;

final class CauseTreeModel implements TreeModel {

	CauseTreeNode root;
	private EnergyStats energyStats;

	CauseTreeModel(EnergyStats energyStats) {
		this.energyStats = energyStats;
		root = energyStats.causeTree.root;
	}

	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object getChild(Object arg0, int arg1) {
		CauseTreeNode node = (CauseTreeNode) arg0;
		return node.children.get(arg1);
	}

	@Override
	public int getChildCount(Object arg0) {
		CauseTreeNode node = (CauseTreeNode) arg0;
		return node.children.size();
	}

	@Override
	public int getIndexOfChild(Object arg0, Object arg1) {
		CauseTreeNode node = (CauseTreeNode) arg0;
		CauseTreeNode child = (CauseTreeNode) arg1;
		return node.children.indexOf(child);
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object arg0) {
		CauseTreeNode node = (CauseTreeNode) arg0;
		return node.children.isEmpty();
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
	}
}