package cwcore.broadcast;

import java.util.ArrayList;
import java.util.List;

import cobweb.Environment.Location;

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
		int pValue;
		for (int i = 0; i < currentPackets.size(); i++) {
			pValue = --currentPackets.get(i).persistence;
			if (pValue <= 0)
				removePacketfromList(currentPackets.get(i));
		}
	}

	/**
	 * returns the packet with the specified ID
	 * 
	 * @return CommPacket
	 */
	public BroadcastPacket getPacket(int packetId) {
		int i = 0;
		while (packetId != (currentPackets.get(i)).packetId & i < currentPackets.size()) {
			i++;
		}
		return currentPackets.get(i);
	}

	/**
	 * removes packets from the list of packets
	 * 
	 * @param packet packet to remove
	 */
	public void removePacketfromList(BroadcastPacket packet /* int packetId */) {
		currentPackets.remove(/* getPacket(packetId) */packet);
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