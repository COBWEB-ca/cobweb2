package org.cobweb.cobweb2.plugins.production;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.plugins.TemporaryEffect;
import org.cobweb.cobweb2.plugins.production.ProductionMapper.ProductionCause;

public class Product implements Drop {
	private final ProductionMapper productionMapper;
	final Location loc;
	private long expiryTime;

	Product(float value, Agent producer, ProductionMapper productionMapper, long expiryPeriod) {
		this.value = value;
		this.producer = producer;
		this.loc = producer.getPosition();
		this.productionMapper = productionMapper;
		this.expiryTime = productionMapper.simulation.getTime() + expiryPeriod;
		productionMapper.updateValues(this, true);
		productionMapper.getAgentState(producer).unsoldProducts++;
	}

	private Agent producer;
	private float value;

	@Override
	public void update() {
		if (productionMapper.simulation.getTime() >= expiryTime)
			productionMapper.remove(this);
	}

	public float getValue() {
		return value;
	}

	@Override
	public boolean canStep(Agent agent) {
		return true;
	}

	@Override
	public void prepareRemove() {
		productionMapper.updateValues(this, false);
		productionMapper.getAgentState(producer).unsoldProducts--;
		this.value = 0;
	}

	public Location getLocation() {
		return loc;
	}

	@Override
	public void onStep(Agent buyer) {
		if (producer != buyer && productionMapper.simulation.getRandom().nextFloat() <= 0.3f) {
			ProductionAgentParams agentParams = productionMapper.getAgentState(producer).agentParams;
			int price = agentParams.productPrice.getValue();

			if (!buyer.enoughEnergy(price))
				return;

			float consumptionRate = buyer.getEnergy() * agentParams.agentConsumptionFactor[buyer.getType()];
			float maxCons = agentParams.agentMaxConsFactor[buyer.getType()];
			if (consumptionRate > maxCons)
				consumptionRate = maxCons;

			if (productionMapper.simulation.getRandom().nextFloat() > consumptionRate)
				return;

			producer.changeEnergy(+price, new ProductSoldCause());
			buyer.changeEnergy(-price, new ProductBoughtCause());

			TemporaryEffect effect = new TemporaryEffect(
					buyer,
					agentParams.productEffect,
					new ProductEffectSource(producer));

			productionMapper.applyEffect(effect);

			productionMapper.remove(this);
		}
	}

	private static class ProductEffectSource {
		private Agent producer;

		public ProductEffectSource(Agent producer) {
			this.producer = producer;
		}

		@Override
		public int hashCode() {
			return producer.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ProductEffectSource) {
				ProductEffectSource other = (ProductEffectSource) obj;
				return this.producer.equals(other.producer);
			}
			return false;
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