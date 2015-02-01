package org.cobweb.cobweb2.plugins.abiotic;

import java.util.Arrays;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.MathUtil;


public class Split extends AbioticFactor {

	@ConfXMLTag("angle")
	@ConfDisplayName("Angle")
	public void setAngle(float value) {
		while (value > 180)
			value -= 360;
		while (value <= -180)
			value += 360;
		angle = value;
	}
	public float getAngle() {
		return angle;
	}
	private float angle = 90f;

	@ConfXMLTag("position")
	@ConfDisplayName("Split Position")
	public float position = 0.3f;

	@ConfXMLTag("sides")
	@ConfDisplayName("Side")
	@ConfList(indexName = "id", startAtOne = true)
	public float[] sides = { 0f, 1f };

	@Override
	public float getValue(float x, float y) {
		// Mathematical angles go counter-clockwise, java goes clockwise
		double rangle = Math.toRadians(-angle);

		// x, y are within [0,1], make them [-.5,.5]
		double dist = MathUtil.getDistanceToLine(x - 0.5, y - 0.5, rangle);

		if (dist > position - .5f)
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
		result.angle = this.angle;
		result.position = this.position;
		return result;
	}

	private static final long serialVersionUID = 1L;
}
