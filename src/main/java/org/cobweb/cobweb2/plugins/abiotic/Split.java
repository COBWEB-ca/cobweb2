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

	@ConfXMLTag("angle")
	@ConfDisplayName("Angle")
	public void setAngle(float value) {
		while (value > 180)
			value -= 360;
		while (value < -180)
			value += 360;
		angle = value;
	}
	public float getAngle() {
		return angle;
	}
	private float angle = 90f;

	@ConfXMLTag("position")
	@ConfDisplayName("Position")
	public float position = 0.3f;

	@Override
	public float getValue(float x, float y) {
		float a = angle;

		// center
		x -= 0.5;
		y -= 0.5;

		float p = position - .5f;

		// Make sure tan(90) is not approached
		float q = a / 45;
		if (q > 1 && q <= 3) {
			// +45 to +135
			float t = x;
			x = -y;
			y = -t;
			a = 90 - a;
		} else if ( q > 3 && q <= 4 || q < -3 && q >= -4 ) {
			// +135 to -135
			x = -x;
			y = -y;
			p = -p;
		} else if ( q > -3 && q <= -1) {
			// -135 to -45
			float t = x;
			x = y;
			y = t;
			p = -p;
			a = 90 - a;
		}

		double rangle = a / 180 * Math.PI;
		double slope = Math.tan(rangle);
		double xZero = -Math.sin(rangle) * p;
		double yZero = Math.cos(rangle) * p;

		if ((y - yZero) > (x - xZero) * slope)
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
