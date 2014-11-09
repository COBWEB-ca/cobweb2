package cobweb;





/**
 * The Agent class represents the physical notion of an Agent in a simulation 
 * (living or not, location, colour).  Instances of the Agent class are not 
 * responsible for implementing the intelligence of the Agent, this is deferred 
 * to the Agent.Controller class.
 */
public abstract class Agent {

	private boolean alive = true;

	protected Agent() {
		id = makeID();
	}

	protected long id;

	private static long nextID = 1;

	private static long makeID() {
		if ((nextID + 1) == Long.MAX_VALUE) {
			nextID = Long.MIN_VALUE;
		}
		return nextID++;
	}

	public static void resetIDSequence() {
		nextID = 1;
	}

	public long getID() {
		return id;
	}

	protected Environment.Location position;

	protected Controller controller;

	protected void init(Controller ai) {
		controller = ai;
		controller.addClientAgent(this); // this currently does absolutely
		// nothing for both simple and
		// complex implementations of
		// controller
	}

	@Override
	public Object clone() {
		try {
			return super.clone(); }
		catch (Exception ex) {
			return null;
		}
		//	return new Object();
	}

	/**
	 * Removes this Agent from the Environment.
	 */
	public void die() {
		assert (isAlive());
		if (!isAlive())
			return;

		position.setAgent(null);
		alive = false;
		controller.removeClientAgent(this);
		position.getEnvironment().getScheduler().removeSchedulerClient(this);
	}

	/**
	 * @return int AgentPDAction
	 */
	public abstract boolean getAgentPDActionCheat();

	public abstract java.awt.Color getColor();

	public Controller getController() {
		return controller;
	}

	public abstract void getDrawInfo(DrawingHandler theUI);

	public int getEnergy() {
		return -1;
	}

	/**
	 * @return the location this Agent occupies.
	 */
	public Environment.Location getPosition() {
		return position;
	}

	public boolean isAlive() {
		return alive;
	}

	/**
	 * Sets the position of the Agent.
	 */
	public void move(Environment.Location newPos) {
		newPos.setAgent(this);
		if (position != null)
			position.setAgent(null);
		position = newPos;
	}


	public abstract void setColor(java.awt.Color c);

	public int type() {
		return -1;
	}
}
