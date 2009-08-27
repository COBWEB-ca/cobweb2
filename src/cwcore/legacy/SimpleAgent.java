package cwcore.legacy;

import java.awt.Color;

import cobweb.DrawingHandler;
import cobweb.Point2D;

@Deprecated
public class SimpleAgent extends cobweb.Agent implements
cobweb.TickScheduler.Client {

	private cobweb.Direction facing = cobweb.Environment.DIRECTION_NORTH;

	public SimpleAgent() {
		super(new SimpleController());
	}

	boolean canStep() {
		cobweb.Environment.Location destPos = getPosition().getAdjacent(facing);
		// The position must be valid...
		if (destPos == null)
			return false;
		// and the destination must be clear of stones
		if (destPos.testFlag(SimpleEnvironment.FLAG_STONE))
			return false;
		// as well as other agents...
		if (destPos.getAgent() != null)
			return false;
		return true;
	}

	@Override
	public int getAgentPDAction() {
		return 0;
	}

	@Override
	public int getAgentPDStrategy() {
		return 0;
	}

	@Override
	public java.awt.Color getColor() {
		return java.awt.Color.black;
	}

	@Override
	public void getDrawInfo(DrawingHandler theUI) {
		theUI.newAgent(java.awt.Color.red, Color.red, java.awt.Color.black,
				new Point2D(getPosition().v[0], getPosition().v[1]),
				new Point2D(facing.v[0], facing.v[1]));
	}

	@Override
	public void setColor(java.awt.Color c) {
		// Nothing
	}

	@Override
	public double similarity(cobweb.Agent other) {
		if (other instanceof SimpleAgent) {
			return 1.0;
		}
		return 0.0;
	}

	@Override
	public double similarity(int other) {
		return 0.0;
	}

	void step() {
		if (canStep())
			move(getPosition().getAdjacent(facing));
	}

	public void tickNotification(long tick) {
		controller.controlAgent(this);
	}

	public void tickZero() {
		// Nothing
	}

	void turnLeft() {
		cobweb.Direction newFacing = new cobweb.Direction(
				2);
		newFacing.v[0] = facing.v[1];
		newFacing.v[1] = -facing.v[0];
		facing = newFacing;
	}

	void turnRight() {
		cobweb.Direction newFacing = new cobweb.Direction(
				2);
		newFacing.v[0] = -facing.v[1];
		newFacing.v[1] = facing.v[0];
		facing = newFacing;
	}
}
