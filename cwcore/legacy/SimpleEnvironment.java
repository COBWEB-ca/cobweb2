package cwcore.legacy;

import driver.Parser;

public class SimpleEnvironment extends cobweb.Environment implements cobweb.TickScheduler.Client {
	public static final int FLAG_STONE = 1;

	// Bitmasks for boolean states
	private static final long MASK_STONE = 1;

	public static void trackAgent(java.io.Writer w) {
		// Nothing
	}

	private int width;

	private int height;

	private long[] locationStates;

	private java.io.PrintWriter logStream;

	@Override
	public void addAgent(int x, int y, int type) {
		// nothing
	}

	@Override
	public void addFood(int x, int y, int type) {
		// nothing
	}

	@Override
	public void addStone(int x, int y) {
		// nothing
	}

	@Override
	public void clearFood() {
		// nothing
	}

	@Override
	public void clearStones() {
		// nothing
	}

	@Override
	public void clearWaste() {
		// nothing
	}

	@Override
	public void fillTileColors(java.awt.Color[] tileColors) {
		int tileIndex = 0;
		for (int x = 0; x < getSize(AXIS_Y); ++x) {
			for (int y = 0; y < getSize(AXIS_X); ++y) {
				Location currentPos = getLocation(x, y);
				if (currentPos.testFlag(FLAG_STONE))
					tileColors[tileIndex++] = java.awt.Color.darkGray;
				else
					tileColors[tileIndex++] = java.awt.Color.white;
			}
		}
	}

	@Override
	public int getAxisCount() {
		return 2;
	}

	@Override
	public boolean getAxisWrap(int axis) {
		return false;
	}

	// Ignored; this model has no fields
	@Override
	protected int getField(cobweb.Environment.Location l, int field) {
		return 0;
	}

	private long getLocationBits(cobweb.Environment.Location l) {
		return locationStates[l.v[0] + l.v[1] * width];
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
	public EnvironmentStats getStatistics() {
		return null;
	}

	@Override
	public int getTypeCount() {
		return 0;
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
			} while ((l.getAgent() != null) || l.testFlag(SimpleEnvironment.FLAG_STONE));
			// new SimpleAgent(l);
		}
	}

	@Override
	public void load(cobweb.Scheduler s, Parser p/* java.io.Reader r */) throws IllegalArgumentException {

		if (s != theScheduler) {
			theScheduler = s;
			s.addSchedulerClient(this);
		}

		// Use this to get the logfile output to the standard console.
		logStream = new java.io.PrintWriter(new java.io.OutputStreamWriter(System.out));
		width = 20;
		height = 20;
		int stoneCount = 20;
		int agentCount = 10;

		width = p.getEnvParams().width;
		height = p.getEnvParams().height;
		stoneCount = p.getEnvParams().initialStones;
		/***********************************************************************
		 * java.io.StreamTokenizer inTokens = new java.io.StreamTokenizer(r); boolean done = false; while (!done) { if
		 * (inTokens.nextToken() != java.io.StreamTokenizer.TT_WORD) throw new java.io.IOException(); if
		 * (inTokens.sval.equals("SimpleEnvironment.End")) { done = true; } else if
		 * (inTokens.sval.equalsIgnoreCase("width")) { if (inTokens.nextToken() != java.io.StreamTokenizer.TT_NUMBER)
		 * throw new java.io.IOException(); width = (int)inTokens.nval; } else if
		 * (inTokens.sval.equalsIgnoreCase("height")) { if (inTokens.nextToken() != java.io.StreamTokenizer.TT_NUMBER)
		 * throw new java.io.IOException(); height = (int)inTokens.nval; } else if
		 * (inTokens.sval.equalsIgnoreCase("stones")) { if (inTokens.nextToken() != java.io.StreamTokenizer.TT_NUMBER)
		 * throw new java.io.IOException(); stoneCount = (int)inTokens.nval; } else if
		 * (inTokens.sval.equalsIgnoreCase("agents")) { if (inTokens.nextToken() != java.io.StreamTokenizer.TT_NUMBER)
		 * throw new java.io.IOException(); agentCount = (int)inTokens.nval; } }
		 **********************************************************************/

		locationStates = new long[width * height];
		// System.out.println("************* Before Init Env Call
		// *************");
		initEnvironment(stoneCount, agentCount);
		// System.out.println("************* After Init Env Call
		// *************");

	}

	@Override
	public void log(java.io.Writer w) {
		logStream = new java.io.PrintWriter(w, false);
	}

	@Override
	public void observe(int x, int y) {
		// nothing
	}

	@Override
	public void removeAgent(int x, int y) {
		// nothing
	}

	@Override
	public void removeFood(int x, int y) {
		// Nothing
	}

	@Override
	public void removeStone(int x, int y) {
		// Nothing
	}

	@Override
	public void report(java.io.Writer w) {
		// Nothing
	}

	@Override
	public void save(java.io.Writer w) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w);
		pw.println(this.getClass().getName());
		pw.println(this.getClass().getName() + ".End");
		pw.flush();
	}

	@Override
	protected void setField(cobweb.Environment.Location l, int field, int value) {
		// nothing
	}

	@Override
	protected void setFlag(cobweb.Environment.Location l, int flag, boolean state) {
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

	private void setLocationBits(cobweb.Environment.Location l, long bits) {
		locationStates[l.v[0] + l.v[1] * width] = bits;
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

	// Handle time
	public void tickNotification(long tick) {
		// Nothing
	}

	public void tickZero() {
		// Nothing
	}

	@Override
	public void unObserve() {
		// Nothing
	}

	@Override
	public void writeLogEntry() {
		if (logStream != null) {
			logStream.println("A tick has passed.");
			logStream.flush();
		}
	}

	public void writeTrackAgentEntry() {
		// nothing
	}
}
