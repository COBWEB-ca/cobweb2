package org.cobweb.cobweb2.plugins.waste;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.Drop;
import org.cobweb.cobweb2.core.Location;


public class Waste implements Drop {

	private int initialWeight;

	private float rate;

	private long birthTick;
	private long expireTick;

	private final double threshold = 0.001;

	public final Location location;

	private WasteMutator wasteManager;

	private int type;

	public Waste(Location loc, int weight, float rate, WasteMutator wasteManager, int type) {
		this.location = loc;
		this.initialWeight = weight;
		this.rate = rate;
		this.wasteManager = wasteManager;
		this.type = type;
		this.birthTick = wasteManager.sim.getTime();
		this.expireTick = birthTick + (long)Math.ceil(Math.log(threshold / initialWeight)/-rate);
	}

	public double getAmount() {
		return initialWeight * Math.exp(-rate * (wasteManager.sim.getTime() - birthTick));
	}

	@Override
	public void update() {
		if (wasteManager.sim.getTime() >= expireTick) {
			wasteManager.remove(this);
		}
	}

	@Override
	public boolean canStep(Agent agent) {
		WasteAgentParams agentParams = wasteManager.getAgentState(agent).agentParams;
		return agentParams.canConsume[type];
	}

	@Override
	public void prepareRemove() {
		// nothing
	}

	@Override
	public void onStep(Agent agent) {
		WasteAgentParams agentParams = wasteManager.getAgentState(agent).agentParams;
		agent.changeEnergy(agentParams.consumeEnergy.getValue(), new WasteConsumptionCause());
		wasteManager.remove(this);
	}

	public static class WasteConsumptionCause implements Cause {

		@Override
		public String getName() {
			return "Waste Consumption";
		}

	}

	@Override
	public int getProducerType() {
		return type;
	}
}
