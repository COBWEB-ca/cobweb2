package org.cobweb.cobweb2.plugins.broadcast;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.core.Location;
import org.cobweb.cobweb2.core.Topology;
import org.cobweb.cobweb2.plugins.EnvironmentMutator;

public class PacketConduit implements EnvironmentMutator {

	private boolean broadcastBlocked = false;

	private Collection<BroadcastPacket> currentPackets = new LinkedList<BroadcastPacket>();

	private Topology topology;

	public void setParams(Topology topo) {
		this.topology = topo;
	}

	/**
	 * adds packets to the list of packets
	 *
	 * @param packet packet
	 */
	public void addPacketToList(BroadcastPacket packet) {
		if (!broadcastBlocked)
			currentPackets.add(packet);
		// TODO: allow more brodcasts?
		blockBroadcast();
	}

	public void blockBroadcast() {
		broadcastBlocked = true;
	}

	// with every time step, the persistence of the packets should be
	// decremented
	public void decrementPersistence() {
		Iterator<BroadcastPacket> i = currentPackets.iterator();
		while (i.hasNext()) {
			BroadcastPacket packet = i.next();
			if (!packet.updateCheckActive())
				i.remove();
		}
	}

	public void unblockBroadcast() {
		broadcastBlocked = false;
	}

	public void clearPackets() {
		currentPackets.clear();
	}

	public BroadcastPacket findPacket(Location position, Agent receiver) {
		// TODO: return more than 1 packet?
		// TODO: return closest packet?
		for (BroadcastPacket commPacket : currentPackets) {
			double distance = topology.getDistance(position, commPacket.location);
			if (distance < commPacket.range
					&& !commPacket.sender.equals(receiver)) {
				return commPacket;
			}
		}
		return null;
	}

	@Override
	public void update() {
		decrementPersistence();
		unblockBroadcast();
	}

	public static class BroadcastCause implements Cause {
		@Override
		public String getName() { return "Broadcast"; }
	}

	public static class BroadcastFoodCause extends BroadcastCause {
		@Override
		public String getName() { return "Broadcast Food"; }
	}

	@Override
	public void loadNew() {
		// nothing
	}

}