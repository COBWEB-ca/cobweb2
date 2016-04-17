package org.cobweb.cobweb2.plugins.production;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.plugins.production.ProductionMapper.ProductionCause;

public class Product implements Drop {
	private final ProductionMapper productionMapper;
	final Location loc;

	Product(float value, Agent owner, Location loc, ProductionMapper productionMapper) {
		this.value = value;
		this.owner = owner;
		this.loc = loc;
		this.productionMapper = productionMapper;
		productionMapper.updateValues(this, true);
	}

	private Agent owner;
	private float value;

	public Agent getOwner() {
		return owner;
	}

	@Override
	public void update() {
		// Nothing, maybe de-value?
	}

	public float getValue() {
		return value;
	}

	@Override
	public boolean canStep() {
		return true;
	}

	@Override
	public void prepareRemove() {
		productionMapper.updateValues(this, false);
		this.value = 0;
	}

	public Location getLocation() {
		return loc;
	}

	@Override
	public void onStep(Agent buyer) {
		if (owner != buyer && productionMapper.simulation.getRandom().nextFloat() <= 0.3f) {
			int price = productionMapper.getAgentState(owner).agentParams.price.getValue();

			if (!buyer.enoughEnergy(price))
				return;

			owner.changeEnergy(+price, new ProductSoldCause());
			buyer.changeEnergy(-price, new ProductBoughtCause());

			productionMapper.remove(this);
		}
	}

	public static class ProductSoldCause extends ProductionCause {
		@Override
		public String getName() { return "Product Sold"; }
	}

	public static class ProductBoughtCause extends ProductionCause {
		@Override
		public String getName() { return "Product Bought"; }
	}

}