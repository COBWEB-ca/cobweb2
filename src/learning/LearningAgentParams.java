package learning;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;


public class LearningAgentParams extends AbstractReflectionParams {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6152370881108746535L;

	public LearningAgentParams() {
	}

	/**
	 * What type of agent this is
	 */
	public int type;

	@ConfXMLTag("BroadcastPleasure")
	@ConfDisplayName("Affection for broadcasting")
	public float broadcastPleasure;

	@ConfXMLTag("FoodPleasure")
	@ConfDisplayName("Affection for eating food")
	public float foodPleasure;	

	@ConfXMLTag("AteAgentPleasure")
	@ConfDisplayName("Affection for eating agents")
	public float ateAgentPleasure;

	@ConfXMLTag("EatAgentEmotionalThreshold")
	@ConfDisplayName("Minimum affection to eat agent")
	public float eatAgentEmotionalThreshold;

	@ConfXMLTag("SparedEmotion")
	@ConfDisplayName("Emotional value when spared")
	public float sparedEmotion;

	@ConfXMLTag("Learns")
	@ConfDisplayName("Agent learns")
	public boolean shouldLearn;

	@ConfXMLTag("LearnFromDifferentOthers")
	@ConfDisplayName("Learn from dissimilar agents")
	public boolean learnFromDifferentOthers;

	@ConfXMLTag("LearnFromOthers")
	@ConfDisplayName("Learn from other agents")
	public boolean learnFromOthers;

	@ConfXMLTag("ChildrenLove")
	@ConfDisplayName("Affection for children")
	public float emotionForChildren;

	@ConfXMLTag("NumMemories")
	@ConfDisplayName("Memories to remember")
	public int numMemories;		

	@ConfXMLTag("PartnerLove")
	@ConfDisplayName("Affection for partner")
	public float loveForPartner;	

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}		
}
