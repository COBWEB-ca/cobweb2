package org.cobweb.cobweb2.plugins.abiotic;


public class HorizontalBands extends Bands {

	@Override
	public float getValue(float x, float y) {
		int band = bandFromPosition(y);
		return bands.get(band).floatValue();
	}

	private static final long serialVersionUID = 1L;
}
