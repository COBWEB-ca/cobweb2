package org.cobweb.cobweb2.ui;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.impl.ComplexEnvironment;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ParameterSerializable;


public class GridStats {

	public CellStats[][] cellStats;

	private int types;

	public GridStats(Simulation sim, Options options) {
		this.types = sim.getAgentTypeCount();
		Topology topo = sim.getTopology();

		cellStats = new CellStats[topo.width / options.cellSize][topo.height / options.cellSize];

		for (int xb = 0; xb < topo.width; xb+= options.cellSize) {
			int w = Math.min(options.cellSize, topo.width - xb);
			int column = xb / options.cellSize;
			for (int yb = 0; yb < topo.height; yb += options.cellSize) {
				int h = Math.min(options.cellSize, topo.height - yb);
				int row = yb / options.cellSize;

				CellStats stats = new CellStats(xb, yb, w, h, sim.theEnvironment);

				cellStats[column][row] = stats;
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

					if (environment.hasFood(l)) {
						foodCount[environment.getFoodType(l)]++;
						if (environment.hasAgent(l))
							agentCount[environment.getAgent(l).getType()]++;
					}
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


	public static class Options implements ParameterSerializable {
		@ConfDisplayName("Cell size")
		public int cellSize;

		private static final long serialVersionUID = 1L;
	}

}
