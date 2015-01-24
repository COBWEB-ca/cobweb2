package org.cobweb.cobweb2.plugins.broadcast;

import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.impl.ComplexAgent;


public class FoodBroadcast extends BroadcastPacket {

	public final Location foodLocation;

	public FoodBroadcast(Location foodLocation, ComplexAgent dispatcherId) {
		super(dispatcherId);
		this.foodLocation = foodLocation;
	}

	@Override
	public void process(ComplexAgent receiver) {
		double closeness = 1;

		if (!foodLocation.equals(receiver.getPosition()))
			closeness = 1 / receiver.environment.topology.getDistance(receiver.getPosition(), foodLocation);

		receiver.setCommInbox(closeness);
	}

}
