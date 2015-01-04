/**
 *
 */
package org.cobweb.cobweb2.core.params;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.interconnect.GeneMutatable;
import org.cobweb.cobweb2.io.AbstractReflectionParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;

/**
 * Parameters for ComplexAgent.
 */
public class ComplexAgentParams extends AbstractReflectionParams {

	private static final long serialVersionUID = -7852361484228627541L;

	/**
	 * Agent type index.
	 */
	@ConfXMLTag("Index")
	public int type = -1;

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
	@GeneMutatable
	public int foodEnergy = 100;

	/**
	 * Energy gained from other food.
	 */
	@ConfDisplayName("Other food energy")
	@ConfXMLTag("OtherFoodEnergy")
	@GeneMutatable
	public int otherFoodEnergy = 25;

	/**
	 * Fraction of energy gained from eating another agent.
	 */
	@ConfDisplayName("Agent eating efficiency")
	@ConfXMLTag("AgentFoodEnergyFraction")
	@GeneMutatable
	public float agentFoodEnergy = 1;

	/**
	 * Amount of energy used to breed.
	 */
	@ConfDisplayName("Breed energy")
	@ConfXMLTag("BreedEnergy")
	@GeneMutatable
	public int breedEnergy = 60;

	/**
	 * Time between asexual breeding and producing child agent.
	 */
	@ConfDisplayName("Asexual pregnancy period")
	@ConfXMLTag("pregnancyPeriod")
	@GeneMutatable
	public int asexPregnancyPeriod = 0;

	/**
	 * Initial energy amount.
	 */
	@ConfDisplayName("Initial energy")
	@ConfXMLTag("InitEnergy")
	public int initEnergy = 100;

	/**
	 * Energy used to step forward.
	 */
	@ConfDisplayName("Step energy")
	@ConfXMLTag("StepEnergy")
	@GeneMutatable
	public int stepEnergy = 1;

	/**
	 * Energy lost bumping into a rock/wall.
	 */
	@ConfDisplayName("Rock bump energy")
	@ConfXMLTag("StepRockEnergy")
	@GeneMutatable
	public int stepRockEnergy = 2;

	/**
	 * Energy lost bumping into another agent.
	 */
	@ConfDisplayName("Agent bump energy")
	@ConfXMLTag("StepAgentEnergy")
	@GeneMutatable
	public int stepAgentEnergy = 2;

	/**
	 * Energy used to turn right.
	 */
	@ConfDisplayName("Turn right energy")
	@ConfXMLTag("TurnRightEnergy")
	public int turnRightEnergy = 1;

	/**
	 * Energy used to turn left.
	 */
	@ConfDisplayName("Turn left energy")
	@ConfXMLTag("TurnLeftEnergy")
	@GeneMutatable
	public int turnLeftEnergy = 1;

	/**
	 * Rate at which the agent's child mutates from the parent.
	 */
	@ConfDisplayName("Mutation rate")
	@ConfXMLTag("MutationRate")
	@GeneMutatable
	public float mutationRate = 0.05f;

	/**
	 * Size of agent's memory in bits.
	 */
	@ConfDisplayName("Memory bits")
	@ConfXMLTag("MemoryBits")
	public int memoryBits = 2;

	/**
	 * Minimum agent similarity for communication to work.
	 */
	@ConfDisplayName("Communication minimum similarity")
	@ConfXMLTag("commSimMin")
	@GeneMutatable
	public int commSimMin = 0;

	/**
	 * Size of communication message in bits.
	 */
	@ConfDisplayName("Communication bits")
	@ConfXMLTag("communicationBits")
	public int communicationBits = 2;

	/**
	 * Chance that bumping into another agent will result in sexual breeding.
	 */
	@ConfDisplayName("Sexual breed chance")
	@ConfXMLTag("sexualBreedChance")
	@GeneMutatable
	public float sexualBreedChance = 1;

	/**
	 * Chance an agent breeds asexually at a time step.
	 */
	@ConfDisplayName("Asexual breed chance")
	@ConfXMLTag("asexualBreedChance")
	@GeneMutatable
	public float asexualBreedChance = 0;

	/**
	 * Minimum agent similarity to be able to breed sexually.
	 */
	@ConfDisplayName("Breeding minimum similarity")
	@ConfXMLTag("breedSimMin")
	@GeneMutatable
	public float breedSimMin = 0;

	/**
	 * Time between sexual breeding and producing child agent.
	 */
	@ConfDisplayName("Sexual pregnancy period")
	@ConfXMLTag("sexualPregnancyPeriod")
	@GeneMutatable
	public int sexualPregnancyPeriod = 5;

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
	@GeneMutatable
	public int agingLimit = 300;

	/**
	 * Age-based energy penalty factor.
	 * agePenalty = agingRate * tan(age / agingLimit * 89.99)
	 */
	@ConfDisplayName("Aging rate")
	@ConfXMLTag("agingRate")
	@GeneMutatable
	public float agingRate = 10;

	/**
	 * Enable waste creation.
	 */
	@ConfDisplayName("Waste")
	@ConfXMLTag("wasteMode")
	public boolean wasteMode = false;

	/**
	 * Energy lost when stepping into waste.
	 */
	@ConfDisplayName("Step waste energy loss")
	@ConfXMLTag("wastePen")
	public int wastePen = 2;

	/**
	 * Waste is produced when this amount of energy is gained.
	 */
	@ConfDisplayName("Waste gain limit")
	@ConfXMLTag("wasteGain")
	public int wasteLimitGain = 100;

	/**
	 * Waste is produced when this amount of energy is lost.
	 */
	@ConfDisplayName("Waste loss limit")
	@ConfXMLTag("wasteLoss")
	public int wasteLimitLoss = 0;

	/**
	 * Waste decay rate.
	 * Formula for decay is: amount = wasteInit * e ^ -rate * time
	 */
	@ConfDisplayName("Waste decay")
	@ConfXMLTag("wasteRate")
	public float wasteDecay = 0.5f;

	/**
	 * Initial waste amount.
	 */
	@ConfDisplayName("Waste initial amount")
	@ConfXMLTag("wasteInit")
	public int wasteInit = 100;

	/**
	 * Use tit-for-tat strategy for prisoner's dilemma.
	 */
	@ConfDisplayName("PD:Use Tit-for-tat")
	@ConfXMLTag("pdTitForTat")
	public boolean pdTitForTat = false;

	/**
	 * Percentage of agents that will be cooperators initially, the rest are cheaters.
	 */
	@ConfDisplayName("PD Cooperation probability")
	@ConfXMLTag("pdCoopProb")
	public int pdCoopProb = 50;

	@ConfDisplayName("PD similarity preference")
	@ConfXMLTag("pdSimilaritySlope")
	@GeneMutatable
	public float pdSimilaritySlope = 0.0f;

	@ConfDisplayName("PD neutral similarity")
	@ConfXMLTag("pdSimilarityNeutral")
	@GeneMutatable
	public float pdSimilarityNeutral = 0.9f;

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
	@GeneMutatable
	public int broadcastFixedRange = 20;

	/**
	 * Minimum agent energy to broadcast.
	 */
	@ConfDisplayName("Broadcast minimum energy")
	@ConfXMLTag("broadcastEnergyMin")
	@GeneMutatable
	public int broadcastEnergyMin = 20;

	/**
	 * Energy used up by broadcasting.
	 */
	@ConfDisplayName("Broadcast cost")
	@ConfXMLTag("broadcastEnergyCost")
	@GeneMutatable
	public int broadcastEnergyCost = 5;

	/**
	 * Agent's food web parameters.
	 */
	@ConfXMLTag("foodweb")
	public FoodwebParams foodweb;

	public ComplexAgentParams(AgentFoodCountable env) {
		foodweb = new FoodwebParams(env);
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void resize(AgentFoodCountable envParams) {
		foodweb.resize(envParams);
	}
}