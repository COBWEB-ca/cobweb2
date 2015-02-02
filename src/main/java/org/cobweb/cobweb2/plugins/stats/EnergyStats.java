package org.cobweb.cobweb2.plugins.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.EnergyMutator;
import org.cobweb.cobweb2.plugins.EnvironmentMutator;
import org.cobweb.cobweb2.plugins.stats.CauseTree.CauseTreeNode;


public class EnergyStats implements EnergyMutator, EnvironmentMutator {

	public float[][] map;
	public float max;
	public float min;
	private Topology topo;

	public static class CauseStats {
		public CauseStats(CauseTreeNode node) {
			this.node = node;
		}
		public int count = 0;
		public double totalDelta = 0;
		public CauseTreeNode node;

		@Override
		public String toString() {
			return node.getName() + " count: " + count + " total: " + totalDelta;
		}
	}

	public Map<Class<? extends Cause>, CauseStats> causeStats = new HashMap<>();

	public CauseTree causeTree = new CauseTree();

	public EnergyStats(Topology topo) {
		this.topo = topo;

		Iterator<CauseTreeNode> iterator = causeTree.iterator();
		while (iterator.hasNext()) {
			CauseTreeNode node = iterator.next();
			CauseStats stats = new CauseStats(node);
			causeStats.put(node.type, stats);
		}
	}

	public boolean collect = false;

	@Override
	public void onEnergyChange(Agent agent, int delta, Cause cause) {
		LocationDirection loc = agent.getPosition();
		if (loc == null)
			return;

		Class<? extends Cause> causeClass = cause.getClass();

		if (!whiteList.isEmpty()) {
			boolean found = false;
			for (Class<? extends Cause> c : whiteList)
				if (c.isAssignableFrom(causeClass)) {
					found = true;
					break;
				}
			if (!found)
				return;
		}

		for (Class<? extends Cause> c : blackList) {
			if (c.isAssignableFrom(causeClass))
				return;
		}

		updateStats(delta, causeClass);

		float newValue = map[loc.x][loc.y] + delta;
		map[loc.x][loc.y] = newValue;

		if (newValue > max)
			max = newValue;
		if (newValue < min)
			min = newValue;
	}

	private void updateStats(int delta, Class<? extends Cause> causeClass) {
		CauseStats stats = causeStats.get(causeClass);
		do {
			stats.count++;
			stats.totalDelta += delta;
			stats = causeStats.get(stats.node.parent.type);
		} while (stats != null);
	}

	@Override
	public void update() {
		map = new float[topo.width][topo.height];
		max = 0;
		min = 0;
	}

	@Override
	public void loadNew() {
		update();
	}

	public List<Class<? extends Cause>> whiteList = new ArrayList<>();

	public List<Class<? extends Cause>> blackList = new ArrayList<>();

}
