package org.cobweb.util;

import java.util.HashMap;
import java.util.Map;


/**
 * Stores a int originalValue, and allows stacking multiplier Factors on top of it.
 * Factors have a Cause attached so they can be updated or removed.
 *
 * @see MutatableFloat
 */
public class MutatableInt {

	private int originalValue;

	public MutatableInt(int value) {
		setValue(value);
	}

	/**
	 * Sets value before any factors are applied to it
	 */
	public void setValue(int value) {
		this.originalValue = value;
	}

	private final Map<Object, Float> multipliers = new HashMap<>();

	/**
	 * Gets value with all the factors applied to it
	 */
	public int getValue() {
		return Math.round(originalValue * multiplierCache);
	}

	/**
	 * Gets value before any factors are applied
	 */
	public int getRawValue() {
		return originalValue;
	}

	/**
	 * Adds/updates a multiplier identified by cause
	 * @param cause The source/cause of factor being applied
	 * @param factor multiplication factor
	 */
	public void setMultiplier(Object cause, float factor) {
		multipliers.put(cause, factor);
		updateCache();
	}

	/**
	 * Removes a multiplier identified by cause
	 * @param cause The source/cause of factor being removed
	 */
	public void removeMultiplier(Object cause) {
		multipliers.remove(cause);
		updateCache();
	}

	private float multiplierCache = 1;

	private void updateCache() {
		multiplierCache = 1;
		for (Float m : multipliers.values())
			multiplierCache *= m;
	}

}
