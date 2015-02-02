package org.cobweb.cobweb2.plugins.abiotic;

import java.util.ArrayList;



public class HorizontalBands extends Bands {

	@Override
	public float getValue(float x, float y) {
		int band = bandFromPosition(y);
		return bands.get(band).floatValue();
	}

	@Override
	public AbioticFactor copy() {
		HorizontalBands result = new HorizontalBands();
		result.bands = new ArrayList<>(this.bands);
		return result;
	}

	@Override
	public String getName() {
		return "Horizontal Bands";
	}

	private static final long serialVersionUID = 1L;
}
