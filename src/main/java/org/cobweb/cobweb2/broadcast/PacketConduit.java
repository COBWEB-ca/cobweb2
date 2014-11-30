package org.cobweb.cobweb2.broadcast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cobweb.cobweb2.core.Location;

public class PacketConduit {

	private boolean broadcastBlocked = false;

	private List<BroadcastPacket> currentPackets = new ArrayList<BroadcastPacket>();

	/**
	 * adds packets to the list of packets
	 * 
	 * @param packet packet
	 */
	public void addPacketToList(BroadcastPacket packet) {
		if (!broadcastBlocked)
			currentPackets.add(packet);
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
			int persistence = packet.persistence--;
			if (persistence <= 0)
				i.remove();
		}
	}

	public void unblockBroadcast() {
		broadcastBlocked = false;
	}

	public void clearPackets() {
		currentPackets.clear();
	}

	public BroadcastPacket findPacket(Location position) {
		for (BroadcastPacket commPacket : currentPackets) {
			if (commPacket.isInRange(position))
				return commPacket;
		}
		return null;
	}

}