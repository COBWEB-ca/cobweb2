package org.cobweb.cobweb2.plugins.pd;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.PerAgentParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;


public class PDParams extends PerAgentParams<PDAgentParams> {
	/**
	 * Agents can play prisoner's dilemma game when they meet.
	 */
	@ConfDisplayName("PD Enabled")
	@ConfXMLTag("enable")
	public boolean enable = false;

	/**
	 * Temptation to defect.
	 */
	@ConfDisplayName("Temptation")
	@ConfXMLTag("temptation")
	public int temptation = 20;

	/**
	 * Reward for mutual cooperation.
	 */
	@ConfDisplayName("Reward")
	@ConfXMLTag("reward")
	public int reward = 10;

	/**
	 * Punishment for mutual defection.
	 */
	@ConfDisplayName("Punishment")
	@ConfXMLTag("punishment")
	public int punishment = 0;

	/**
	 * Sucker's payoff
	 */
	@ConfDisplayName("Sucker's payoff")
	@ConfXMLTag("sucker")
	public int sucker = -5;



	public PDParams(AgentFoodCountable size) {
		super(PDAgentParams.class, size);
	}

	@Override
	protected PDAgentParams newAgentParam() {
		return new PDAgentParams();
	}

	private static final long serialVersionUID = 2L;
}
