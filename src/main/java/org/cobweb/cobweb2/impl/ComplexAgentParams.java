/**
 *
 */
package org.cobweb.cobweb2.impl;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.Mutatable;
import org.cobweb.cobweb2.plugins.PerTypeParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

/**
 * Parameters for ComplexAgent.
 */
public class ComplexAgentParams implements ParameterSerializable, PerTypeParam {

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
	@Mutatable
	public int foodEnergy = 100;

	/**
	 * Energy gained from other food.
	 */
	@ConfDisplayName("Other food energy")
	@ConfXMLTag("OtherFoodEnergy")
	@Mutatable
	public int otherFoodEnergy = 25;

	/**
	 * Fraction of energy gained from eating another agent.
	 */
	@ConfDisplayName("Agent eating efficiency")
	@ConfXMLTag("AgentFoodEnergyFraction")
	@Mutatable
	public float agentFoodEnergy = 1;

	/**
	 * Amount of energy used to breed.
	 */
	@ConfDisplayName("Breed energy")
	@ConfXMLTag("BreedEnergy")
	@Mutatable
	public int breedEnergy = 60;

	/**
	 * Time between asexual breeding and producing child agent.
	 */
	@ConfDisplayName("Asexual pregnancy period")
	@ConfXMLTag("pregnancyPeriod")
	@Mutatable
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
	@Mutatable
	public int stepEnergy = 1;

	/**
	 * Energy lost bumping into a rock/wall.
	 */
	@ConfDisplayName("Rock bump energy")
	@ConfXMLTag("StepRockEnergy")
	@Mutatable
	public int stepRockEnergy = 2;

	/**
	 * Energy lost bumping into another agent.
	 */
	@ConfDisplayName("Agent bump energy")
	@ConfXMLTag("StepAgentEnergy")
	@Mutatable
	public int stepAgentEnergy = 2;

	/**
	 * Energy used to turn right.
	 */
	@ConfDisplayName("Turn right energy")
	@ConfXMLTag("TurnRightEnergy")
	@Mutatable
	public int turnRightEnergy = 1;

	/**
	 * Energy used to turn left.
	 */
	@ConfDisplayName("Turn left energy")
	@ConfXMLTag("TurnLeftEnergy")
	@Mutatable
	public int turnLeftEnergy = 1;

	/**
	 * Rate at which the agent's child mutates from the parent.
	 */
	@ConfDisplayName("Mutation rate")
	@ConfXMLTag("MutationRate")
	@Mutatable
	public float mutationRate = 0.05f;

	/**
	 * Minimum agent similarity for communication to work.
	 */
	@ConfDisplayName("Communication minimum similarity")
	@ConfXMLTag("commSimMin")
	@Mutatable
	public int commSimMin = 0;

	/**
	 * Chance that bumping into another agent will result in sexual breeding.
	 */
	@ConfDisplayName("Sexual breed chance")
	@ConfXMLTag("sexualBreedChance")
	@Mutatable
	public float sexualBreedChance = 1;

	/**
	 * Chance an agent breeds asexually at a time step.
	 */
	@ConfDisplayName("Asexual breed chance")
	@ConfXMLTag("asexualBreedChance")
	@Mutatable
	public float asexualBreedChance = 0;

	/**
	 * Minimum agent similarity to be able to breed sexually.
	 */
	@ConfDisplayName("Breeding minimum similarity")
	@ConfXMLTag("breedSimMin")
	@Mutatable
	public float breedSimMin = 0;

	/**
	 * Time between sexual breeding and producing child agent.
	 */
	@ConfDisplayName("Sexual pregnancy period")
	@ConfXMLTag("sexualPregnancyPeriod")
	@Mutatable
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
	@Mutatable
	public int agingLimit = 300;

	/**
	 * Age-based energy penalty factor.
	 * agePenalty = agingRate * tan(age / agingLimit * 89.99)
	 */
	@ConfDisplayName("Aging rate")
	@ConfXMLTag("agingRate")
	@Mutatable
	public float agingRate = 10;

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
	@Mutatable
	public int broadcastFixedRange = 20;

	/**
	 * Minimum agent energy to broadcast.
	 */
	@ConfDisplayName("Broadcast minimum energy")
	@ConfXMLTag("broadcastEnergyMin")
	@Mutatable
	public int broadcastEnergyMin = 20;

	/**
	 * Energy used up by broadcasting.
	 */
	@ConfDisplayName("Broadcast cost")
	@ConfXMLTag("broadcastEnergyCost")
	@Mutatable
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
	public ComplexAgentParams clone() {
		try {
			return (ComplexAgentParams) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		foodweb.resize(envParams);
	}
}