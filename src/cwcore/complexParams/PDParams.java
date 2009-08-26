package cwcore.complexParams;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;

public class PDParams extends AbstractReflectionParams {

	private static final long serialVersionUID = 1380425322335531943L;

	@ConfDisplayName("Temptation")
	@ConfXMLTag("temptation")
	public int temptation;

	@ConfDisplayName("Reward")
	@ConfXMLTag("reward")
	public int reward;

	@ConfDisplayName("Punishment")
	@ConfXMLTag("punishment")
	public int punishment;

	@ConfDisplayName("Sucker's payoff")
	@ConfXMLTag("sucker")
	public int sucker;

	public PDParams() {
		temptation = 20;
		reward = 10;
		punishment = 0;
		sucker = -5;
	}
}
