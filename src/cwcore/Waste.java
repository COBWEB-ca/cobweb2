package cwcore;

import java.awt.Color;

import cobweb.CellObject;


public class Waste extends Drop {

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
	public void reset(long newBirthTick, int weight, float newRate) {
		initialWeight = weight;
		this.birthTick = newBirthTick;
		this.rate = newRate;
		// to avoid recalculating every tick
		threshold = 0.001 * initialWeight; // changed from 0.5 to 0.001 by
		// skinawy
		valid = true;
	}

	final static Color WASTE_COLOR = new Color(238, 201, 0);

	@Override
	public Color getColor() {
		return WASTE_COLOR;
	}

	@Override
	public boolean canStep() {
		return false;
	}

	/**
	 * Waste can't move.
	 */
	@Override
	public boolean canMove() {
		return false;
	}

	/**
	 * Nothing can go on top of waste.
	 * @param other Another cell object.
	 * @return False.
	 */
	@Override
	public boolean canCoverWith(CellObject other) {
		return false;
	}

	@Override
	public void onStep(ComplexAgent agent) {
		throw new IllegalStateException("Agents can't step on waste");
	}
}