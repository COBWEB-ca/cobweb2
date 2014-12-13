package org.cobweb.cobweb2.production;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.ComplexEnvironment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.interconnect.StateParameter;
import org.cobweb.cobweb2.interconnect.StatePlugin;

public class ProductionMapper implements StatePlugin {

	private ComplexEnvironment e;
	private float[][] vals;
	private float maxValue;
	SimulationInternals simulation;

	public ProductionMapper(SimulationInternals sim) {
		this.e = (ComplexEnvironment) sim.getEnvironment();
		simulation = sim;
		vals = new float[this.e.getWidth()][this.e.getHeight()];
		params = new LinkedList<StateParameter>();
		params.add(new ProductHunt());
	}

	void addProduct(Product p, Location loc) {
		float newMax = 0;
		for (int x = 0; x < vals.length; x++) {
			for (int y = 0; y < vals[x].length; y++) {
				float value = getDifAtLoc(p, loc, e.getLocation(x, y));
				vals[x][y] += value;
				if (vals[x][y] > newMax) {
					newMax = vals[x][y];
				}
			}
		}
		maxValue = newMax;
	}

	void remProduct(Product p) {
		Location loc = p.loc;
		e.setDrop(loc, null);
		e.setFlag(loc, ComplexEnvironment.FLAG_DROP, false);

		float newMax = 0;
		for (int x = 0; x < vals.length; x++) {
			for (int y = 0; y < vals[x].length; y++) {
				float value = getDifAtLoc(p, p.getLocation(), e.getLocation(x, y));
				vals[x][y] -= value;
				if (vals[x][y] > newMax) {
					newMax = vals[x][y];
				}
			}
		}
		maxValue = newMax;
	}

	private float getDifAtLoc(Product source, Location loc, Location loc2) {
		float val = source.getValue();
		val /= Math.max(1, loc.distanceSquared(loc2));
		return val;
	}

	/**
	 * @param loc - the location whose "Productivity value" we are querying
	 * @return the total "Productivity value" of the parameter Location.
	 *
	 *         It is most efficient to place products on tiles that have prod.
	 *         vals. that indicate that a sufficient number of products are
	 *         nearby in order to attract agent's business, but not enough are
	 *         around so that there is too much competition. Therefore:
	 *
	 *         -An agent's probability of dropping a product on a tile with low
	 *         (~0) prod. val. should be low. (but not non-existant because then
	 *         initially agents would never drop products) -An agent's
	 *         probability of dropping a product on a tile with a very high
	 *         prod. val. should be infinitesimal. -An agent should have a high
	 *         chance of dropping a product on a tile with a moderate prob. val.
	 *
	 */
	public float getValueAtLocation(Location loc) {
		return vals[loc.x][loc.y];
	}

	public float getValueAtLocation(int x, int y) {
		Location loc = e.getLocation(x, y);
		return getValueAtLocation(loc);
	}

	public Color[][] getTileColors(int x, int y) {
		Color[][] ret = new Color[x][y];
		float lockedMax = maxValue;

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				float val = getValueAtLocation(i, j);
				int amount = 255 - (int) ((Math.min(val, lockedMax) / lockedMax) * 255f);
				// FIXME: threading bug when speed set to max simulation speed, amount ends up out of range
				if (amount > 255) {
					amount = 255;
				}

				ret[i][j] = new Color(amount / 2 + 127, amount, 255);
			}
		}
		return ret;
	}

	public class ProductHunt implements StateParameter {

		@Override
		public String getName() {
			return "ProdHunt";
		}

		@Override
		public double getValue(ComplexAgent agent) {
			Location here = agent.getPosition();
			Location ahead = e.getAdjacent(here, agent.getFacing());
			if (ahead == null || !e.isValidLocation(ahead)) {
				return 0;
			}

			float a = getValueAtLocation(here);
			float b = getValueAtLocation(ahead);

			float max = Math.max(a, b);
			if (max == 0)
				return 0;

			return b / max;
		}

	}

	private List<StateParameter> params;

	@Override
	public List<StateParameter> getParameters() {
		return params;
	}

	Set<Product> products = new LinkedHashSet<Product>();

	public Product createProduct(float value, ComplexAgent owner) {
		Location loc = owner.getPosition();
		Product prod = new Product(value, owner, loc, this);
		addProduct(prod, loc);

		e.setDrop(loc, prod);
		products.add(prod);

		return prod;
	}


	private boolean roll(float chance) {
		return chance > simulation.getRandom().nextFloat();
	}

	boolean shouldProduce(ComplexAgent agent) {
		if (agent.prodParams == null || !roll(agent.prodParams.initProdChance)) {
			return false;
		}

		float locationValue = getValueAtLocation(agent.getPosition());

		if (locationValue > agent.prodParams.highDemandCutoff) {
			return false;
		}

		// ADDITIONS:
		// Learning agents should adapt to products

		if (locationValue <= agent.prodParams.lowDemandThreshold) {
			// In an area of low demand
			return roll(agent.prodParams.lowDemandProdChance);
		} else if (locationValue <= agent.prodParams.sweetDemandThreshold) {
			/*
			 * The sweet spot is an inverted parabola, the vertex is 100% probability in the middle of the sweet spot
			 * (between lowDemandThreshold and sweetDemandThreshold)
			 * the tips are sweetDemandStartChance probability at the thresholds.
			 *
			 */

			// parabola shape
			float peak = (agent.prodParams.lowDemandThreshold + agent.prodParams.sweetDemandThreshold) * 0.5f;
			float width = agent.prodParams.sweetDemandThreshold - agent.prodParams.lowDemandThreshold;
			// position along standard parabola
			float x = (locationValue - peak) / (width / 2);
			// parabola value
			float y = x * x  * (1-agent.prodParams.sweetDemandStartChance);

			float chance = agent.prodParams.sweetDemandStartChance + (1 - y);

			// Sweet spot; perfect balance of competition and attraction here;
			// likelihood of producing products here
			// is modelled by a parabola
			return roll(chance);
		}

		// locationValue > 10f; Very high competition in this area!
		// The higher the value the lower the production chances are.

		// Let: d = prodParams.sweetDemandThreshold
		// e = prodParams.highDemandCutoff
		// f = prodParams.highDemandProdChance
		//
		// p1 = (d, f);
		// p2 = (e, 0);
		//
		// rise = f - 0 = f;
		// run = d - e
		//
		// m = f / (d - e)
		//
		// y = mx + b
		//
		// b = y - mx
		// b = 0 - me
		// b = -(f / (d -e))e
		//
		// y = ((f - e) / d)x + e

		float d = agent.prodParams.sweetDemandThreshold;
		float e = agent.prodParams.highDemandCutoff;
		float f = agent.prodParams.highDemandProdChance;

		float rise = f;
		float run = d - e;

		float m = rise / run;

		float b = -1 * m * e;

		// y = mx + b
		float y = (m * locationValue) + b;

		// p1 = (sweetDemandThreshold, prodParams.highDemandProdChance)
		// p2 = (
		// minChance

		return roll(y);
	}

	public void tryProduction(ComplexAgent agent) {
		if (agent.prodParams.productionMode && shouldProduce(agent)) {
			// TODO: find a more clean way to create and assign product
			// Healthy agents produce high-value products, and vice-versa
			createProduct(agent.getEnergy() / (float) agent.params.initEnergy, agent);

			if (e.testFlag(agent.getPosition(), ComplexEnvironment.FLAG_FOOD)) {
				e.setFlag(agent.getPosition(), ComplexEnvironment.FLAG_FOOD, false);
			}

			e.setFlag(agent.getPosition(), ComplexEnvironment.FLAG_DROP, true);

		}
	}


}
