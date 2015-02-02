package org.cobweb.cobweb2.plugins.stats;

import java.util.ArrayList;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.EnergyMutator;
import org.cobweb.cobweb2.plugins.EnvironmentMutator;


public class EnergyStats implements EnergyMutator, EnvironmentMutator {

	public float[][] map;
	public float max;
	public float min;
	private Topology topo;

	public EnergyStats(Topology topo) {
		this.topo = topo;
	}

	public boolean collect = false;

	@Override
	public void onEnergyChange(Agent agent, int delta, Cause cause) {
		LocationDirection loc = agent.getPosition();
		if (loc == null)
			return;

		if (!whiteList.isEmpty()) {
			boolean found = false;
			for (Class<? extends Cause> c : whiteList)
				if (c.isAssignableFrom(cause.getClass())) {
					found = true;
					break;
				}
			if (!found)
				return;
		}

		for (Class<? extends Cause> c : blackList) {
			if (c.isAssignableFrom(cause.getClass()))
				return;
		}

		float newValue = map[loc.x][loc.y] + delta;
		map[loc.x][loc.y] = newValue;

		if (newValue > max)
			max = newValue;
		if (newValue < min)
			min = newValue;
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
