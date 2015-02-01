package org.cobweb.cobweb2.plugins.abiotic;

import java.util.ArrayList;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.MathUtil;


public class AngleBands extends Bands {

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

	@Override
	public float getValue(float x, float y) {
		// Mathematical angles go counter-clockwise, java goes clockwise
		double rangle = Math.toRadians(-angle);

		// x, y are within [0,1], make them [-.5,.5]
		double dist = MathUtil.pointLineDistInSquare(x - 0.5, y - 0.5, rangle);

		double indexD = (dist + 0.5) * bands.size();
		int index = (int) indexD;
		if (index < 0)
			index = 0;
		if (index >= bands.size())
			index = bands.size() - 1;

		return bands.get(index);
	}

	@Override
	public String getName() {
		return "Angled Bands";
	}

	@Override
	public AbioticFactor copy() {
		AngleBands result = new AngleBands();
		result.bands = new ArrayList<>(this.bands);
		result.angle = this.angle;
		return result;
	}

	private static final long serialVersionUID = 1L;
}
