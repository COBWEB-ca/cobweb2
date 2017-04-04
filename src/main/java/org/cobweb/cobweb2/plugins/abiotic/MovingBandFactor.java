package org.cobweb.cobweb2.plugins.abiotic;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.MathUtil;


public class MovingBandFactor extends CyclicFactor {


	@ConfXMLTag("bgValue")
	@ConfDisplayName("Background value")
	public float bgValue = 0f;

	@ConfXMLTag("bandValue")
	@ConfDisplayName("Band value")
	public float bandValue = 1f;

	@ConfXMLTag("bandWidth")
	@ConfDisplayName("Band width")
	public float bandWidth = 0.15f;

	@ConfXMLTag("transition")
	@ConfDisplayName("Edge hardness")
	public float transition = 4f;

	@ConfXMLTag("posStart")
	@ConfDisplayName("Start position")
	public float posStart = 0.2f;

	@ConfXMLTag("posEnd")
	@ConfDisplayName("End position")
	public float posEnd = 1f;

	@ConfXMLTag("wrap")
	@ConfDisplayName("Wrap")
	public boolean wrap = false;

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
		float z = (float) MathUtil.pointLineDistInSquare(x - .5, y - .5, Math.toRadians(-angle)) + .5f;
		float p = time * (posEnd - posStart) + posStart;

		float distance = Math.abs(p - z);

		if (wrap && distance > 0.5f)
			distance = 1f - distance;

		distance -= bandWidth / 2;

		float t =  1.0f / (float) (1 + Math.exp(-distance * 10 * transition));
		t = MathUtil.clamp(t, 0, 1);

		return bgValue * t + bandValue * (1 - t);
	}

	@Override
	public float getMax() {
		return Math.max(bgValue, bandValue);
	}

	@Override
	public float getMin() {
		return Math.min(bgValue, bandValue);
	}

	@Override
	public String getName() {
		return "Moving Band";
	}

	@Override
	public AbioticFactor copy() {
		try {
			MovingBandFactor copy = (MovingBandFactor) super.clone();
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}


	private static final long serialVersionUID = 1L;
}
