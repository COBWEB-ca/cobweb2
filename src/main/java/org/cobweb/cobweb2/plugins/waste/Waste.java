package org.cobweb.cobweb2.plugins.waste;

import org.cobweb.cobweb2.core.Agent;
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

	public Waste(Location loc, int weight, float rate, WasteMutator wasteManager) {
		this.location = loc;
		this.initialWeight = weight;
		this.rate = rate;
		this.wasteManager = wasteManager;
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
	public boolean canStep() {
		return false;
	}

	@Override
	public void prepareRemove() {
		// nothing
	}

	@Override
	public void onStep(Agent agent) {
		throw new IllegalStateException("Agents can't step on waste");
	}
}
