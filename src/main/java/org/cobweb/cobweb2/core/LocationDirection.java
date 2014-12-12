package org.cobweb.cobweb2.core;


public class LocationDirection extends Location {
	public final Direction direction;

	public LocationDirection(Location l, Direction d) {
		super(l.x, l.y);
		direction = d;
	}

	private static final long serialVersionUID = 2L;
}
