package org.cobweb.cobweb2.plugins.abiotic;

import java.util.Arrays;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.MathUtil;


public class AngleGradient extends AbioticFactor {

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

	@ConfXMLTag("values")
	@ConfDisplayName("Value")
	@ConfList(indexName = "id", startAtOne = true)
	public float[] values = { 0f, 1f };

	@Override
	public float getValue(float x, float y) {
		// Mathematical angles go counter-clockwise, java goes clockwise
		double rangle = Math.toRadians(-angle);

		// x, y are within [0,1], make them [-.5,.5]
		double dist = MathUtil.pointLineDistInSquare(x - 0.5, y - 0.5, rangle);

		float partOne = (float) (dist + 0.5);
		if (partOne < 0)
			partOne = 0;
		if (partOne > 1)
			partOne = 1;

		return values[0] * partOne + values[1] * (1 - partOne);
	}


	@Override
	public float getMax() {
		return Math.max(values[0], values[1]);
	}

	@Override
	public float getMin() {
		return Math.min(values[0], values[1]);
	}

	@Override
	public String getName() {
		return "Angled Gradient";
	}

	@Override
	public AbioticFactor copy() {
		AngleGradient result = new AngleGradient();
		result.values = Arrays.copyOf(this.values, values.length);
		result.angle = this.angle;
		return result;
	}

	private static final long serialVersionUID = 1L;
}
