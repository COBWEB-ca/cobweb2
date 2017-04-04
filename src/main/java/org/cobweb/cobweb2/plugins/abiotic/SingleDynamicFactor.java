package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.MathUtil;


public class SingleDynamicFactor extends CyclicFactor {

	@ConfXMLTag("valueMin")
	@ConfDisplayName("Min value")
	public float valueMin = 0;

	@ConfXMLTag("valueMax")
	@ConfDisplayName("Max value")
	public float valueMax = 1;

	@Override
	public float getValue(float x, float y) {
		return MathUtil.linearInterpolation(time, 0, 1, valueMin, valueMax);
	}

	@Override
	public float getMax() {
		return valueMax;
	}

	@Override
	public float getMin() {
		return valueMin;
	}

	@Override
	public String getName() {
		return "Single Dynamic";
	}

	@Override
	public SingleDynamicFactor copy() {
		try {
			SingleDynamicFactor copy = (SingleDynamicFactor) super.clone();
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
