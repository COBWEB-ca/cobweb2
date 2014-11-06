package cwcore.complexParams;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;

/**
 * Prisoner's Dilemma parameters
 */
public class PDParams extends AbstractReflectionParams {

	private static final long serialVersionUID = 1380425322335531943L;

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

	public PDParams() {
		// public, no parameter constructor for serialization
	}
}
