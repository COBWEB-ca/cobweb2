/**
 *
 */
package org.cobweb.cobweb2.impl;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.plugins.ResizableParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.CloneHelper;
import org.cobweb.util.MutatableFloat;
import org.cobweb.util.MutatableInt;

/**
 * Parameters for ComplexAgent.
 */
public class ComplexAgentParams implements ResizableParam {

	private static final long serialVersionUID = -7852361484228627541L;

	/**
	 * Initial number of agents.
	 */
	@ConfDisplayName("Initial count")
	@ConfXMLTag("Agents")
	public int initialAgents = 20;

	/**
	 * Energy gained from favourite food.
	 */
	@ConfDisplayName("Favourite food energy")
	@ConfXMLTag("FoodEnergy")
	public MutatableInt foodEnergy = new MutatableInt(100);

	/**
	 * Energy gained from other food.
	 */
	@ConfDisplayName("Other food energy")
	@ConfXMLTag("OtherFoodEnergy")
	public MutatableInt otherFoodEnergy = new MutatableInt(25);

	/**
	 * Fraction of energy gained from eating another agent.
	 */
	@ConfDisplayName("Agent eating efficiency")
	@ConfXMLTag("AgentFoodEnergyFraction")
	public MutatableFloat agentFoodEnergy = new MutatableFloat(1);

	/**
	 * Amount of energy used to breed.
	 */
	@ConfDisplayName("Breed energy")
	@ConfXMLTag("BreedEnergy")
	public MutatableInt breedEnergy = new MutatableInt(60);

	/**
	 * Time between asexual breeding and producing child agent.
	 */
	@ConfDisplayName("Asexual pregnancy period")
	@ConfXMLTag("pregnancyPeriod")
	public MutatableInt asexPregnancyPeriod = new MutatableInt(0);

	/**
	 * Initial energy amount.
	 */
	@ConfDisplayName("Initial energy")
	@ConfXMLTag("InitEnergy")
	public MutatableInt initEnergy = new MutatableInt(100);

	/**
	 * Energy used to step forward.
	 */
	@ConfDisplayName("Step energy")
	@ConfXMLTag("StepEnergy")
	public MutatableInt stepEnergy = new MutatableInt(1);

	/**
	 * Energy lost bumping into a rock/wall.
	 */
	@ConfDisplayName("Rock bump energy")
	@ConfXMLTag("StepRockEnergy")
	public MutatableInt stepRockEnergy = new MutatableInt(2);

	/**
	 * Energy lost bumping into another agent.
	 */
	@ConfDisplayName("Agent bump energy")
	@ConfXMLTag("StepAgentEnergy")
	public MutatableInt stepAgentEnergy = new MutatableInt(2);

	/**
	 * Energy used to turn right.
	 */
	@ConfDisplayName("Turn right energy")
	@ConfXMLTag("TurnRightEnergy")
	public MutatableInt turnRightEnergy = new MutatableInt(1);

	/**
	 * Energy used to turn left.
	 */
	@ConfDisplayName("Turn left energy")
	@ConfXMLTag("TurnLeftEnergy")
	public MutatableInt turnLeftEnergy = new MutatableInt(1);

	/**
	 * Rate at which the agent's child mutates from the parent.
	 */
	@ConfDisplayName("Mutation rate")
	@ConfXMLTag("MutationRate")
	public MutatableFloat mutationRate = new MutatableFloat(0.05f);

	/**
	 * Minimum agent similarity for communication to work.
	 */
	@ConfDisplayName("Communication minimum similarity")
	@ConfXMLTag("commSimMin")
	public MutatableInt commSimMin = new MutatableInt(0);

	/**
	 * Chance that bumping into another agent will result in sexual breeding.
	 */
	@ConfDisplayName("Sexual breed chance")
	@ConfXMLTag("sexualBreedChance")
	public MutatableFloat sexualBreedChance = new MutatableFloat(1);

	/**
	 * Chance an agent breeds asexually at a time step.
	 */
	@ConfDisplayName("Asexual breed chance")
	@ConfXMLTag("asexualBreedChance")
	public MutatableFloat asexualBreedChance = new MutatableFloat(0);

	/**
	 * Minimum agent similarity to be able to breed sexually.
	 */
	@ConfDisplayName("Breeding minimum similarity")
	@ConfXMLTag("breedSimMin")
	public MutatableFloat breedSimMin = new MutatableFloat(0);

	/**
	 * Time between sexual breeding and producing child agent.
	 */
	@ConfDisplayName("Sexual pregnancy period")
	@ConfXMLTag("sexualPregnancyPeriod")
	public MutatableInt sexualPregnancyPeriod = new MutatableInt(5);

	/**
	 * Enable aging mode.
	 */
	@ConfDisplayName("Aging")
	@ConfXMLTag("agingMode")
	public boolean agingMode = false;

	/**
	 * Age limit after which the agent is forced to die.
	 */
	@ConfDisplayName("Age limit")
	@ConfXMLTag("agingLimit")
	public MutatableInt agingLimit = new MutatableInt(300);

	/**
	 * Age-based energy penalty factor.
	 * agePenalty = agingRate * tan(age / agingLimit * 89.99)
	 */
	@ConfDisplayName("Aging rate")
	@ConfXMLTag("agingRate")
	public MutatableFloat agingRate = new MutatableFloat(10);

	/**
	 * How many PD cheaters an agent will remember.
	 */
	@ConfDisplayName("PD memory size")
	@ConfXMLTag("pdMemorySize")
	public int pdMemory = 10;

	/**
	 * Enables message broadcasts.
	 */
	@ConfDisplayName("Broadcast")
	@ConfXMLTag("broadcastMode")
	public boolean broadcastMode = false;

	/**
	 * Makes broadcast radius depend on agent energy.
	 * Formula is: radius = energy / 10 + 1.
	 */
	@ConfDisplayName("Broadcast energy-based")
	@ConfXMLTag("broadcastEnergyBased")
	public boolean broadcastEnergyBased = false;

	/**
	 * Radius of broadcast area.
	 */
	@ConfDisplayName("Broadcast fixed range")
	@ConfXMLTag("broadcastFixedRange")
	public MutatableInt broadcastFixedRange = new MutatableInt(20);

	/**
	 * Minimum agent energy to broadcast.
	 */
	@ConfDisplayName("Broadcast minimum energy")
	@ConfXMLTag("broadcastEnergyMin")
	public MutatableInt broadcastEnergyMin = new MutatableInt(20);

	/**
	 * Energy used up by broadcasting.
	 */
	@ConfDisplayName("Broadcast cost")
	@ConfXMLTag("broadcastEnergyCost")
	public MutatableInt broadcastEnergyCost = new MutatableInt(5);

	@ConfDisplayName("Broadcast heard only by same type")
	@ConfXMLTag("broadcastSameTypeOnly")
	public boolean broadcastSameTypeOnly = false;

	@ConfDisplayName("Broadcast listener minimum similarity")
	@ConfXMLTag("broadcastMinSimilarity")
	public MutatableFloat broadcastMinSimilarity = new MutatableFloat(0f);

	/**
	 * The possible partner type of the agent
	 */
	@ConfDisplayName("Partner of other type")
	@ConfXMLTag("partnerType")
	public MutatableInt partnerType = new MutatableInt(-1);

	/**
	 * The possible agent type that this agent can give birth to
	 */
	@ConfDisplayName("Possible child type")
	@ConfXMLTag("childType")
	public MutatableInt childType = new MutatableInt(-1);

	/**
	 * The probability of giving birth to an agent of "possible child type"
     * e.g. if probGiveBirthToOtherType = 0.8, then prob. of giving birth to
     * "possible child type" is 0.8, the prob. of giving birth to this agent type
     * is 0.1, the prob. of giving birth to its partner type is 0.1.
	 */
	@ConfDisplayName("Probability of giving birth to child")
	@ConfXMLTag("ProbGiveBirthToOtherType")
	public MutatableFloat probGiveBirthToOtherType = new MutatableFloat(0);

	/**
	 * Agent's food web parameters.
	 */
	@ConfXMLTag("foodweb")
	public FoodwebParams foodweb;

	public ComplexAgentParams(AgentFoodCountable env) {
		foodweb = new FoodwebParams(env);
	}

	@Override
	public ComplexAgentParams clone() {
		try {
			ComplexAgentParams copy = (ComplexAgentParams) super.clone();
			CloneHelper.resetMutatable(copy);
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		foodweb.resize(envParams);
	}
}