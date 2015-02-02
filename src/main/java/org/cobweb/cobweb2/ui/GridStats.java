package org.cobweb.cobweb2.ui;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ParameterSerializable;


public class GridStats {

	public CellStats[][] cellStats;

	public int types;

	public RegionOptions opts;

	public GridStats(Simulation sim, RegionOptions opts) {
		this.opts = opts;
		this.types = sim.getAgentTypeCount();
		Topology topo = sim.getTopology();

		cellStats = new CellStats[opts.hDivisions][opts.vDivisions];

		for (int xi = 0; xi < opts.hDivisions; xi++) {
			int xb = topo.width * xi / opts.hDivisions;
			int w = topo.width * (xi + 1) / opts.hDivisions - xb;

			for (int yi = 0; yi < opts.vDivisions; yi++) {
				int yb = topo.height * yi / opts.vDivisions;
				int h = topo.height * (yi + 1) / opts.vDivisions - yb;

				CellStats stats = new CellStats(xb, yb, w, h, sim.theEnvironment);

				cellStats[xi][yi] = stats;
			}
		}
	}

	public class CellStats {
		public int xb;
		public int yb;
		public int w;
		public int h;

		public CellStats(int xb, int yb, int w, int h, ComplexEnvironment environment) {
			this.xb = xb;
			this.yb = yb;
			this.w = w;
			this.h = h;

			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					Location l = new Location(xb + x, yb + y);

					if (environment.hasFood(l))
						foodCount[environment.getFoodType(l)]++;

					if (environment.hasAgent(l))
						agentCount[environment.getAgent(l).getType()]++;
				}
			}
		}

		public int[] agentCount = new int[types];
		public int[] foodCount = new int[types];

		public int totalAgents() {
			int total = 0;
			for (int i = 0; i < types; i++)
				total += agentCount[i];
			return total;
		}

		public int totalFood() {
			int total = 0;
			for (int i = 0; i < types; i++)
				total += foodCount[i];
			return total;
		}

		public int area() {
			return w * h;
		}
	}


	public static class RegionOptions implements ParameterSerializable {
		@ConfDisplayName("Horizontal Divisions")
		public int hDivisions = 5;

		@ConfDisplayName("Vertical Divisions")
		public int vDivisions = 5;

		private static final long serialVersionUID = 1L;
	}

}
