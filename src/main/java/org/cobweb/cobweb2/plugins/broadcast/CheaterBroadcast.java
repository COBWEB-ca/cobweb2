package org.cobweb.cobweb2.plugins.broadcast;

import org.cobweb.cobweb2.impl.ComplexAgent;


public class CheaterBroadcast extends BroadcastPacket {

	public final ComplexAgent cheater;

	public CheaterBroadcast(ComplexAgent cheater, ComplexAgent dispatcherId) {
		super(dispatcherId);
		this.cheater = cheater;
	}

	@Override
	public void process(ComplexAgent receiver) {
		receiver.rememberCheater(cheater);
	}

}
