package org.cobweb.cobweb2.ui.swing.energy;

import org.cobweb.cobweb2.plugins.stats.CauseTree;
import org.cobweb.cobweb2.plugins.stats.CauseTree.CauseTreeNode;
import org.cobweb.cobweb2.plugins.stats.EnergyStats;
import org.cobweb.cobweb2.plugins.stats.EnergyStats.CauseStats;
import org.jdesktop.swingx.treetable.TreeTableModel;


public class CauseStatsTreeTableModel extends CauseTreeModel implements TreeTableModel {

	private EnergyStats stats;

	CauseStatsTreeTableModel(CauseTree tree, EnergyStats stats) {
		super(tree);
		this.stats = stats;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == 1)
			return int.class;
		if (col == 2)
			return double.class;

		return null;
	}

	@Override
	public String getColumnName(int col) {
		if (col == 1)
			return "Count";
		if (col == 2)
			return "Total Change";

		return null;
	}

	@Override
	public int getHierarchicalColumn() {
		return 0;
	}

	@Override
	public Object getValueAt(Object row, int col) {
		assert col != 0;
		CauseTreeNode node = (CauseTreeNode) row;
		CauseStats nodeStats = stats.causeStats.get(node.type);
		if (col == 1)
			return nodeStats.count;
		if (col == 2)
			return nodeStats.totalDelta;

		return null;
	}

	@Override
	public boolean isCellEditable(Object row, int col) {
		return false;
	}

	@Override
	public void setValueAt(Object row, Object value, int col) {
		throw new UnsupportedOperationException();
	}


}
