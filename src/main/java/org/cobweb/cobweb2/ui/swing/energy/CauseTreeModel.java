package org.cobweb.cobweb2.ui.swing.energy;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.cobweb.cobweb2.plugins.stats.CauseTree;
import org.cobweb.cobweb2.plugins.stats.CauseTree.CauseTreeNode;

public class CauseTreeModel implements TreeModel {

	CauseTreeNode root;

	CauseTreeModel(CauseTree tree) {
		root = tree.root;
	}

	List<TreeModelListener> listeners = new ArrayList<>();

	public void fireNodeChanged(TreePath path) {
		for (TreeModelListener l : listeners) {
			l.treeNodesChanged(new TreeModelEvent(this, path));
		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener arg0) {
		listeners.add(arg0);
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
		listeners.remove(l);
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
	}
}