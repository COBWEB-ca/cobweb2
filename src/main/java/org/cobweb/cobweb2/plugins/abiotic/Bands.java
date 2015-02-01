package org.cobweb.cobweb2.plugins.abiotic;

import java.util.ArrayList;
import java.util.List;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfListType;
import org.cobweb.io.ConfXMLTag;



public abstract class Bands extends AbioticFactor {

	@ConfDisplayName("Band")
	@ConfXMLTag("Bands")
	@ConfList(indexName = "Band", startAtOne = true)
	@ConfListType(float.class)
	public List<Float> bands = new ArrayList<Float>();

	public Bands() {
		bands.add(0f);
		bands.add(1f);
	}

	protected int bandFromPosition(float floatPosition) {
		int size = bands.size();
		int band = (int)(size * floatPosition);
		return band;
	}

	@Override
	public float getMax() {
		float max = Float.NEGATIVE_INFINITY;
		for (Float b : bands) {
			if (b.floatValue() > max)
				max = b;
		}
		return max;
	}

	@Override
	public float getMin() {
		float min = Float.POSITIVE_INFINITY;
		for (Float b : bands) {
			if (b.floatValue() < min)
				min = b;
		}
		return min;
	}

	private static final long serialVersionUID = 1L;
}
