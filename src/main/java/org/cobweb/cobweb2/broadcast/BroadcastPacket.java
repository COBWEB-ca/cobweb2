package org.cobweb.cobweb2.broadcast;

import org.cobweb.cobweb2.core.Environment;
import org.cobweb.cobweb2.core.Location;

public class BroadcastPacket {

	private static final int DEFAULT = 1;

	public static final int FOOD = 1;

	public static final int CHEATER = 2;

	private int packetCounter;

	int packetId; // Unique ID for each communication packet

	private int type; // Type of packet (Food or ). This is an enumerated
	// number that can be extended

	private long dispatcherId; // ID of sending agent (or entity)...could
	// be modified to take an Object type

	// According to this sender ID, other members may decide whether or not
	// to accept this message
	private String content; // Content of message. e.g. Cheater with ID 1234
	// encountered...migth be enumerated

	// e.g. Food found at location 34,43
	private int radius; // Reach over the whole environment or just a
	// certain neighborhood

	int persistence; // how many time steps should the packet

	private final Location location;

	private Environment environment;

	/**
	 * Constructor
	 *
	 *
	 */
	public BroadcastPacket(int type, long dispatcherId, String content, int energy, boolean energyBased, int fixedRange, Location location, Environment env) {

		this.location = location;
		this.environment = env;
		this.packetId = ++packetCounter;
		this.type = type;
		this.dispatcherId = dispatcherId;
		this.content = content; // could be a message or an enumerated type
		// depending on the type
		if (energyBased)
			this.radius = getRadius(energy);
		else
			this.radius = fixedRange;
		this.persistence = DEFAULT;
	}

	// stay there. By default, a packet will
	// persist for one time step (value 1)

	public String getContent() {
		return content;
	}

	public long getDispatcherId() {
		return dispatcherId;
	}

	public int getPacketId() {
		return packetId;
	}

	public int getPersistence() {
		return persistence;
	}

	public int getRadius() {
		return radius;
	}

	private static int getRadius(int energy) {
		return energy / 10 + 1; // limiting minimum to 1 unit of
		// radius
	}

	public int getType() {
		return type;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setDispatcherId(int dispatcherId) {
		this.dispatcherId = dispatcherId;
	}

	public void setPacketId(int packetId) {
		this.packetId = packetId;
	}

	public void setPersistence(int persistence) {
		this.persistence = persistence;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isInRange(Location position) {
		return environment.getDistance(this.location, position)< radius;
	}
}