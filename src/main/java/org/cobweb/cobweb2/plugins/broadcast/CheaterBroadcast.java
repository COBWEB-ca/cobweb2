package org.cobweb.cobweb2.plugins.broadcast;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.impl.ComplexAgent;


public class CheaterBroadcast extends BroadcastPacket {

	public final Agent cheater;

	public CheaterBroadcast(Agent cheater, ComplexAgent dispatcherId) {
		super(dispatcherId);
		this.cheater = cheater;
	}

	@Override
	public void process(ComplexAgent receiver) {
		receiver.rememberBadAgent(cheater);
	}

}
