package org.cobweb.cobweb2.plugins.broadcast;

import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.impl.ComplexAgent;

public abstract class BroadcastPacket {

	public final ComplexAgent sender;
	// According to this sender ID, other members may decide whether or not
	// to accept this message

	// e.g. Food found at location 34,43
	public final int range; // Reach over the whole environment or just a
	// certain neighborhood

	public final Location location;

	public BroadcastPacket(ComplexAgent dispatcherId) {
		this.sender = dispatcherId;
		this.location = dispatcherId.getPosition();

		if (sender.params.broadcastEnergyBased)
			this.range = getRadius(sender.getEnergy());
		else
			this.range = sender.params.broadcastFixedRange.getValue();
	}

	private static int getRadius(int energy) {
		return energy / 10 + 1; // limiting minimum to 1 unit of
		// radius
	}

	public abstract void process(ComplexAgent receiver);

	private int persistence = 1;

	public boolean updateCheckActive() {
		return persistence-- >= 0;
	}
}