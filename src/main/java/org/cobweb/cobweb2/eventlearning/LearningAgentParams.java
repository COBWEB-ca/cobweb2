package org.cobweb.cobweb2.eventlearning;

import org.cobweb.cobweb2.io.AbstractReflectionParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;


public class LearningAgentParams extends AbstractReflectionParams {
	/**
	 *
	 */
	private static final long serialVersionUID = 6152370881108746535L;

	@ConfXMLTag("BroadcastPleasure")
	@ConfDisplayName("Affection for broadcasting")
	public float broadcastPleasure = 0.1f;

	@ConfXMLTag("FoodPleasure")
	@ConfDisplayName("Affection for eating food")
	public float foodPleasure = 0.5f;

	@ConfXMLTag("AteAgentPleasure")
	@ConfDisplayName("Affection for eating agents")
	public float ateAgentPleasure = 0.2f;

	@ConfXMLTag("EatAgentEmotionalThreshold")
	@ConfDisplayName("Minimum affection to eat agent")
	public float eatAgentEmotionalThreshold = 0.1f;

	@ConfXMLTag("SparedEmotion")
	@ConfDisplayName("Emotional value when spared")
	public float sparedEmotion = 0.8f;

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
	public float emotionForChildren = 0;

	@ConfXMLTag("NumMemories")
	@ConfDisplayName("Memories to remember")
	public int numMemories = 4;

	@ConfXMLTag("PartnerLove")
	@ConfDisplayName("Affection for partner")
	public float loveForPartner = -0.1f;

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}
}
