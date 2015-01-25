package org.cobweb.cobweb2.plugins.production;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.core.Location;

public class Product implements Drop {
	private final ProductionMapper productionMapper;
	final Location loc;

	Product(float value, Agent owner, Location loc, ProductionMapper productionMapper) {
		this.value = value;
		this.owner = owner;
		this.loc = loc;
		this.productionMapper = productionMapper;
	}

	private Agent owner;
	private float value;

	public Agent getOwner() {
		return owner;
	}

	@Override
	public boolean isActive(long val) {
		return true;
	}

	public float getValue() {
		return value;
	}

	@Override
	public boolean canStep() {
		return true;
	}

	@Override
	public void expire() {
		productionMapper.remProduct(this);
	}

	public Location getLocation() {
		return loc;
	}

	@Override
	public void onStep(Agent agent) {
		if (owner != agent && productionMapper.simulation.getRandom().nextFloat() <= 0.3f) {
			productionMapper.remProduct(this);

			// TODO: Reward p.owner for selling his product!
			// Reward this agent for buying a product! (and punish it
			// for paying for it?)
			// Should agents have currency to use for buying products?
			//
			// FIXME: set up product cost/benefit
			owner.changeEnergy(0, new ProductSoldCause());
			agent.changeEnergy(0, new ProductBoughtCause());

		}
	}

	public static class ProductSoldCause implements Cause {
		@Override
		public String getName() { return "Product Sold"; }
	}

	public static class ProductBoughtCause implements Cause {
		@Override
		public String getName() { return "Product Bought"; }
	}

}