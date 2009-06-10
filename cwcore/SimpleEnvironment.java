package cwcore;

import driver.Parser;

public class SimpleEnvironment extends cobweb.Environment implements
		cobweb.TickScheduler.Client {
	public static final int FLAG_STONE = 1;

	public SimpleEnvironment(cobweb.Scheduler s, Parser p/* java.io.Reader r */) {
		super(s);
		try {
			load(s, p);
		} catch (java.io.IOException e) {
			throw new InstantiationError(
					"Error parsing Simple Environment parameters.");
		}
	}

	private void initEnvironment(int stoneCount, int agentCount) {
		// System.out.println("************* Inside InitEnv *************");
		for (int i = 0; i < stoneCount; ++i) {
			cobweb.Environment.Location l;
			do {
				l = getRandomLocation();
			} while (l.testFlag(SimpleEnvironment.FLAG_STONE));
			l.setFlag(SimpleEnvironment.FLAG_STONE, true);
		}
		for (int i = 0; i < agentCount; ++i) {
			cobweb.Environment.Location l;
			do {
				l = getRandomLocation();
			} while ((l.getAgent() != null)
					|| l.testFlag(SimpleEnvironment.FLAG_STONE));
			// new SimpleAgent(l);
		}
	}

	@Override
	public void selectStones(int x, int y, cobweb.UIInterface ui) {
	}

	@Override
	public void selectFood(int x, int y, int type, cobweb.UIInterface theUI) {
	}

	@Override
	public void selectAgent(int x, int y, int type, cobweb.UIInterface theUI) {
	}

	@Override
	public void remove(int mode, cobweb.UIInterface ui) {
	}

	@Override
	public void save(java.io.Writer w) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w);
		pw.println(this.getClass().getName());
		pw.println(this.getClass().getName() + ".End");
		pw.flush();
	}

	@Override
	public void log(java.io.Writer w) {
		logStream = new java.io.PrintWriter(w, false);
	}

	@Override
	public void report(java.io.Writer w) {
	}

	@Override
	public void writeLogEntry() {
		if (logStream != null) {
			logStream.println("A tick has passed.");
			logStream.flush();
		}
	}

	public static void trackAgent(java.io.Writer w) {
	}

	public void writeTrackAgentEntry() {
	}

	@Override
	public void load(cobweb.Scheduler s, Parser p/* java.io.Reader r */)
			throws java.io.IOException {

		if (s != theScheduler) {
			theScheduler = s;
			s.addSchedulerClient(this);
		}

		// Use this to get the logfile output to the standard console.
		logStream = new java.io.PrintWriter(new java.io.OutputStreamWriter(
				System.out));
		width = 20;
		height = 20;
		int stoneCount = 20;
		int agentCount = 10;

		width = p.Width[0];
		height = p.Height[0];
		stoneCount = p.randomStones[0];
		/***********************************************************************
		 * java.io.StreamTokenizer inTokens = new java.io.StreamTokenizer(r);
		 * boolean done = false; while (!done) { if (inTokens.nextToken() !=
		 * java.io.StreamTokenizer.TT_WORD) throw new java.io.IOException(); if
		 * (inTokens.sval.equals("SimpleEnvironment.End")) { done = true; } else
		 * if (inTokens.sval.equalsIgnoreCase("width")) { if
		 * (inTokens.nextToken() != java.io.StreamTokenizer.TT_NUMBER) throw new
		 * java.io.IOException(); width = (int)inTokens.nval; } else if
		 * (inTokens.sval.equalsIgnoreCase("height")) { if (inTokens.nextToken() !=
		 * java.io.StreamTokenizer.TT_NUMBER) throw new java.io.IOException();
		 * height = (int)inTokens.nval; } else if
		 * (inTokens.sval.equalsIgnoreCase("stones")) { if (inTokens.nextToken() !=
		 * java.io.StreamTokenizer.TT_NUMBER) throw new java.io.IOException();
		 * stoneCount = (int)inTokens.nval; } else if
		 * (inTokens.sval.equalsIgnoreCase("agents")) { if (inTokens.nextToken() !=
		 * java.io.StreamTokenizer.TT_NUMBER) throw new java.io.IOException();
		 * agentCount = (int)inTokens.nval; } }
		 **********************************************************************/

		locationStates = new long[width * height];
		// System.out.println("************* Before Init Env Call
		// *************");
		initEnvironment(stoneCount, agentCount);
		// System.out.println("************* After Init Env Call
		// *************");

	}

	@Override
	public int getAxisCount() {
		return 2;
	}

	@Override
	public int getSize(int axis) {
		switch (axis) {
		case AXIS_X:
			return width;
		case AXIS_Y:
			return height;
		default:
			return 0;
		}
	}

	@Override
	public boolean getAxisWrap(int axis) {
		return false;
	}

	// Handle time
	public void tickNotification(long tick) {
	}

	@Override
	public void fillTileColors(java.awt.Color[] tileColors) {
		Location currentPos = getLocation(0, 0);
		int tileIndex = 0;
		for (; currentPos.v[1] < getSize(AXIS_Y); ++currentPos.v[1]) {
			for (currentPos.v[0] = 0; currentPos.v[0] < getSize(AXIS_X); ++currentPos.v[0]) {
				if (currentPos.testFlag(FLAG_STONE))
					tileColors[tileIndex++] = java.awt.Color.darkGray;
				else
					tileColors[tileIndex++] = java.awt.Color.white;
			}
		}
	}

	// Hidden implementation stuff...
	@Override
	protected boolean testFlag(cobweb.Environment.Location l, int flag) {
		switch (flag) {
		case FLAG_STONE:
			return ((getLocationBits(l) & MASK_STONE) == MASK_STONE);
		default:
			return false;
		}
	}

	@Override
	protected void setFlag(cobweb.Environment.Location l, int flag,
			boolean state) {
		switch (flag) {
		case FLAG_STONE:
			if (state)
				setLocationBits(l, getLocationBits(l) | MASK_STONE);
			else
				setLocationBits(l, getLocationBits(l) & ~MASK_STONE);
			break;
		default:
		}
	}

	@Override
	public int getTypeCount() {
		return 0;
	}

	// Ignored; this model has no fields
	@Override
	protected int getField(cobweb.Environment.Location l, int field) {
		return 0;
	}

	@Override
	protected void setField(cobweb.Environment.Location l, int field, int value) {
	}

	// Bitmasks for boolean states
	private static final long MASK_STONE = 1;

	private long getLocationBits(cobweb.Environment.Location l) {
		return locationStates[l.v[0] + l.v[1] * width];
	}

	private void setLocationBits(cobweb.Environment.Location l, long bits) {
		locationStates[l.v[0] + l.v[1] * width] = bits;
	}

	private int width;

	private int height;

	private long[] locationStates;

	private java.io.PrintWriter logStream;

	@Override
	public void setclick(int i) {
	}

	@Override
	public void observe(int x, int y, cobweb.UIInterface ui) {
	}

	@Override
	public EnvironmentStats getStatistics() {
		// TODO Auto-generated method stub
		return null;
	};
}
