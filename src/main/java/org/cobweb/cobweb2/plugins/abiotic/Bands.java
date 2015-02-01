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
		bands.add(0f);
	}

	protected int bandFromPosition(float floatPosition) {
		int size = bands.size();
		int band = (int)(size * floatPosition);
		return band;
	}

	private static final long serialVersionUID = 1L;
}
