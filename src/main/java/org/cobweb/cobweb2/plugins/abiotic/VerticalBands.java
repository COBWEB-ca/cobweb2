package org.cobweb.cobweb2.plugins.abiotic;

import java.util.ArrayList;


public class VerticalBands extends Bands {

	@Override
	public float getValue(float x, float y) {
		int band = bandFromPosition(x);
		return bands.get(band).floatValue();
	}

	@Override
	public AbioticFactor copy() {
		VerticalBands result = new VerticalBands();
		result.bands = new ArrayList<>(this.bands);
		return result;
	}

	@Override
	public String getName() {
		return "Vertical Bands";
	}

	private static final long serialVersionUID = 1L;
}
