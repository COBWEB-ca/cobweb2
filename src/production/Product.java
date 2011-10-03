package production;

import java.awt.Color;

import cobweb.Agent;
import cwcore.ComplexEnvironment.Drop;

public class Product implements Drop {
	public Product(float value, Agent owner) {
		this.value = value;
		this.owner = owner;
	}

	Agent owner;
	private float value;

	public Agent getOwner() {
		return owner;
	}

	@Override
	public boolean isActive(long val) {
		return true;
	}

	@Override
	public void reset(long time, int weight, float rate) {

	}

	public void setValue(float value) {
		this.value = value;
	}

	public float getValue() {
		return value;
	}

	private static final Color MY_COLOR = new Color(128, 0, 255);

	@Override
	public Color getColor() {
		return MY_COLOR;
	}

	@Override
	public boolean canStep() {
		return true;
	}
}