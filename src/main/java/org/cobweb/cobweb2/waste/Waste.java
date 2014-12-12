package org.cobweb.cobweb2.waste;

import org.cobweb.cobweb2.core.ComplexAgent;
import org.cobweb.cobweb2.core.Drop;


public class Waste implements Drop {

	private int initialWeight;

	private long birthTick;

	private float rate;

	private double threshold;

	private boolean valid;

	public Waste() {
		this(0, 0, 0);
	}

	public Waste(long birthTick, int weight, float rate) {
		super();
		initialWeight = weight;
		this.birthTick = birthTick;
		this.rate = rate;
		// to avoid recalculating every tick
		threshold = 0.001 * initialWeight; // changed from 0.5 to 0.001 by
		// skinawy
		this.valid = true;
	}

	/* Call me sparsely, especially when the age is really old */
	public double getAmount(long tick) {
		return initialWeight * Math.pow(Math.E, -rate * (tick - birthTick));
	}

	@Override
	public boolean isActive(long tick) {
		if (!valid)
			return false;
		if (getAmount(tick) < threshold) {
			valid = false;
			return false;
		}
		return true;
	}

	@Override
	public boolean canStep() {
		return false;
	}

	@Override
	public void expire() {
		// nothing so far
	}

	@Override
	public void onStep(ComplexAgent agent) {
		throw new IllegalStateException("Agents can't step on waste");
	}
}
