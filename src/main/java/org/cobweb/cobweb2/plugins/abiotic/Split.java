package org.cobweb.cobweb2.plugins.abiotic;

import java.util.Arrays;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;


public class Split extends AbioticFactor {

	@ConfXMLTag("sides")
	@ConfDisplayName("Side")
	@ConfList(indexName = "id", startAtOne = true)
	public float[] sides = { 0f, 1f };

	@ConfXMLTag("vertical")
	@ConfDisplayName("Vertical")
	public boolean vertical = false;

	@ConfXMLTag("position")
	@ConfDisplayName("Position")
	public float position = 0.3f;

	@Override
	public float getValue(float x, float y) {
		float f = vertical ? x : y;
		if (f >= position)
			return sides[1];
		else
			return sides[0];
	}

	@Override
	public float getMax() {
		return Math.max(sides[0], sides[1]);
	}

	@Override
	public float getMin() {
		return Math.min(sides[0], sides[1]);
	}

	@Override
	public String getName() {
		return "Split";
	}

	@Override
	public AbioticFactor copy() {
		Split result = new Split();
		result.sides = Arrays.copyOf(this.sides, sides.length);
		result.vertical = this.vertical;
		result.position = this.position;
		return result;
	}

	private static final long serialVersionUID = 1L;
}
