package org.cobweb.cobweb2.plugins.production;

import java.util.Arrays;
import java.util.List;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.LocationDirection;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.core.StateParameter;
import org.cobweb.cobweb2.core.StatePlugin;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.StatefulSpawnMutatorBase;
import org.cobweb.cobweb2.plugins.UpdateMutator;
import org.cobweb.util.ArrayUtilities;

public class ProductionMapper extends StatefulSpawnMutatorBase<ProductionAgentParams> implements StatePlugin, UpdateMutator {

	private Environment environment;
	private float[][] vals;
	private float maxValue;
	SimulationInternals simulation;
	private ProductionAgentParams[] initialParams;

	public ProductionMapper(SimulationInternals sim) {
		super(ProductionAgentParams.class, sim);
		simulation = sim;
	}

	void remProduct(Product p) {
		Location loc = p.loc;
		environment.removeDrop(loc);

		updateValues(p, false);
	}

	private void updateValues(Product p, boolean addition) {
		float newMax = 0;
		for (int x = 0; x < vals.length; x++) {
			for (int y = 0; y < vals[x].length; y++) {
				float value = getDifAtLoc(p, new Location(x, y));
				vals[x][y] += addition ? value : - value;

				if (vals[x][y] < 0) {
					vals[x][y] = 0;
				}
				if (vals[x][y] > newMax) {
					newMax = vals[x][y];
				}
			}
		}

		// Accumulation errors could make this a very small number,
		// and we only care about real values
		if (newMax < 1)
			newMax = 1;

		maxValue = newMax;
	}

	private float getDifAtLoc(Product source, Location loc2) {
		float val = source.getValue();
		val /= Math.max(1, simulation.getTopology().getDistanceSquared(source.getLocation(), loc2));
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
	private float getValueAtLocation(Location loc) {
		return vals[loc.x][loc.y];
	}

	public float[][] getValues() {
		return vals;
	}

	public float getMax() {
		return maxValue;
	}

	private class ProductHunt implements StateParameter {

		@Override
		public String getName() {
			return ProductionParams.STATE_NAME_PRODHUNT;
		}

		@Override
		public double getValue(Agent agent) {
			LocationDirection here = agent.getPosition();
			Location ahead = simulation.getTopology().getAdjacent(here);
			if (ahead == null || !simulation.getTopology().isValidLocation(ahead)) {
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

	@Override
	public List<StateParameter> getParameters() {
		return Arrays.asList(
				(StateParameter)new ProductHunt());
	}

	private void addProduct(float value, Agent owner) {
		Location loc = owner.getPosition();
		Product prod = new Product(value, owner, loc, this);

		updateValues(prod, true);

		owner.changeEnergy(-getAgentState(owner).productionCost, new ProduceProductCause());

		environment.addDrop(loc, prod);
	}


	private boolean roll(float chance) {
		return chance > simulation.getRandom().nextFloat();
	}

	private boolean shouldProduce(Agent agent) {
		if (!agent.isAlive() || !hasAgentState(agent)) {
			return false;
		}

		ProductionAgentParams params = getAgentState(agent);
		if (!params.productionMode || !roll(params.initProdChance)){
			return false;
		}

		LocationDirection loc = agent.getPosition();
		if (environment.hasDrop(loc))
			return false;

		float locationValue = getValueAtLocation(loc);

		if (locationValue > params.highDemandCutoff) {
			return false;
		}


		// ADDITIONS:
		// Learning agents should adapt to products

		if (locationValue <= params.lowDemandThreshold) {
			// In an area of low demand
			return roll(params.lowDemandProdChance);
		} else if (locationValue <= params.sweetDemandThreshold) {
			/*
			 * The sweet spot is an inverted parabola, the vertex is 100% probability in the middle of the sweet spot
			 * (between lowDemandThreshold and sweetDemandThreshold)
			 * the tips are sweetDemandStartChance probability at the thresholds.
			 *
			 */

			// parabola shape
			float peak = (params.lowDemandThreshold + params.sweetDemandThreshold) * 0.5f;
			float width = params.sweetDemandThreshold - params.lowDemandThreshold;
			// position along standard parabola
			float x = (locationValue - peak) / (width / 2);
			// parabola value
			float y = x * x  * (1-params.sweetDemandStartChance);

			float chance = params.sweetDemandStartChance + (1 - y);

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

		float d = params.sweetDemandThreshold;
		float e = params.highDemandCutoff;
		float f = params.highDemandProdChance;

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

	@Override
	public void onUpdate(Agent agent) {
		if (shouldProduce(agent)) {
			// TODO: find a more clean way to create and assign product
			// Healthy agents produce high-value products, and vice-versa
			addProduct(agent.getEnergy() / (float) ((ComplexAgent)agent).params.initEnergy, agent);
		}
	}


	@Override
	public ProductionAgentParams stateForNewAgent(Agent agent) {
		return initialParams[agent.getType()].clone();
	}

	@Override
	protected ProductionAgentParams stateFromParent(Agent agent, ProductionAgentParams parentState) {
		if (parentState == null)
			return null;
		return parentState.clone();
	}

	public void setParams(ProductionParams productionParams) {
		initialParams = productionParams.agentParams;
	}

	public void initEnvironment(Environment env, boolean keepOldProducts) {
		environment = env;

		// FIXME: this has to happen after environment is set up, since simulation.getTopology comes from the environment
		if (vals == null || !keepOldProducts) {
			vals = new float[simulation.getTopology().width][simulation.getTopology().height];
		} else {
			vals = ArrayUtilities.resizeArray(vals, simulation.getTopology().width, simulation.getTopology().height);
		}
	}


	public static class ProduceProductCause implements Cause {
		@Override
		public String getName() { return "Produce Product"; }
	}

}
