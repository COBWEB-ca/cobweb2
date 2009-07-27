/**
 *
 */
package cwcore.complexParams;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;


public class ComplexAgentParams extends AbstractReflectionParams implements Cloneable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7852361484228627541L;

	@ConfXMLTag("Index")
	public int type;

	@ConfDisplayName("Initial count")
	@ConfXMLTag("Agents")
	public int initialAgents;

	@ConfDisplayName("Favorite food energy")
	@ConfXMLTag("FoodEnergy")
	@GeneMutatable
	public int foodEnergy;

	@ConfDisplayName("Other food energy")
	@ConfXMLTag("OtherFoodEnergy")
	@GeneMutatable
	public int otherFoodEnergy;

	@ConfDisplayName("Breed energy")
	@ConfXMLTag("BreedEnergy")
	@GeneMutatable
	public int breedEnergy;

	@ConfDisplayName("Pregnancy period")
	@ConfXMLTag("pregnancyPeriod")
	@GeneMutatable
	public int pregnancyPeriod;

	@ConfDisplayName("Initial energy")
	@ConfXMLTag("InitEnergy")
	public int initEnergy;

	@ConfDisplayName("Step energy")
	@ConfXMLTag("StepEnergy")
	@GeneMutatable
	public int stepEnergy;

	@ConfDisplayName("Rock bump energy")
	@ConfXMLTag("StepRockEnergy")
	@GeneMutatable
	public int stepRockEnergy;

	@ConfDisplayName("Agent bump energy")
	@ConfXMLTag("StepAgentEnergy")
	@GeneMutatable
	public int stepAgentEnergy;

	@ConfDisplayName("Turn right energy")
	@ConfXMLTag("TurnRightEnergy")
	public int turnRightEnergy;

	@ConfDisplayName("Turn left energy")
	@ConfXMLTag("TurnLeftEnergy")
	@GeneMutatable
	public int turnLeftEnergy;

	@ConfDisplayName("Mutation rate")
	@ConfXMLTag("MutationRate")
	@GeneMutatable
	public float mutationRate;

	@ConfDisplayName("Memory bits")
	@ConfXMLTag("MemoryBits")
	public int memoryBits;

	@ConfDisplayName("Communication minimum similarity")
	@ConfXMLTag("commSimMin")
	@GeneMutatable
	public int commSimMin;

	@ConfDisplayName("Communication bits")
	@ConfXMLTag("communicationBits")
	public int communicationBits;

	@ConfDisplayName("Sexual breed chance")
	@ConfXMLTag("sexualBreedChance")
	@GeneMutatable
	public float sexualBreedChance;

	@ConfDisplayName("Asexual breed chance")
	@ConfXMLTag("asexualBreedChance")
	@GeneMutatable
	public float asexualBreedChance;

	@ConfDisplayName("Breeding minimum similarity")
	@ConfXMLTag("breedSimMin")
	@GeneMutatable
	public float breedSimMin;

	@ConfDisplayName("Sexual pregnancy period")
	@ConfXMLTag("sexualPregnancyPeriod")
	@GeneMutatable
	public int sexualPregnancyPeriod;

	@ConfDisplayName("Aging")
	@ConfXMLTag("agingMode")
	public boolean agingMode;

	@ConfDisplayName("Age limit")
	@ConfXMLTag("agingLimit")
	@GeneMutatable
	public int agingLimit;

	@ConfDisplayName("Aging rate")
	@ConfXMLTag("agingRate")
	@GeneMutatable
	public float agingRate;

	@ConfDisplayName("Waste")
	@ConfXMLTag("wasteMode")
	public boolean wasteMode;

	@ConfDisplayName("Step waste energy loss")
	@ConfXMLTag("wastePen")
	public int wastePen;

	@ConfDisplayName("Waste gain limit")
	@ConfXMLTag("wasteGain")
	public int wasteLimitGain;

	@ConfDisplayName("Waste loss limit")
	@ConfXMLTag("wasteLoss")
	public int wasteLimitLoss;

	@ConfDisplayName("Waste decay")
	@ConfXMLTag("wasteRate")
	public float wasteDecay;

	@ConfDisplayName("Waste initial ammount")
	@ConfXMLTag("wasteInit")
	public int wasteInit;

	@ConfDisplayName("PD:Use Tit-for-tat")
	@ConfXMLTag("pdTitForTat")
	public boolean pdTitForTat;

	@ConfDisplayName("PD Cooperation probability")
	@ConfXMLTag("pdCoopProb")
	public int pdCoopProb;

	@ConfDisplayName("PD memory size")
	@ConfXMLTag("pdMemorySize")
	public int pdMemory;

	@ConfDisplayName("Broadcast")
	@ConfXMLTag("broadcastMode")
	public boolean broadcastMode;

	@ConfDisplayName("Broadcast energy-based")
	@ConfXMLTag("broadcastEnergyBased")
	public boolean broadcastEnergyBased;

	@ConfDisplayName("Broadcast fixed range")
	@ConfXMLTag("broadcastFixedRange")
	@GeneMutatable
	public int broadcastFixedRange;

	@ConfDisplayName("Broadcast minumum energy")
	@ConfXMLTag("broadcastEnergyMin")
	@GeneMutatable
	public int broadcastEnergyMin;

	@ConfDisplayName("Broadcast cost")
	@ConfXMLTag("broadcastEnergyCost")
	@GeneMutatable
	public int broadcastEnergyCost;

	public String genetic_sequence = "001110010011100100111001";

	@ConfXMLTag("foodweb")
	public FoodwebParams foodweb;

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	public ComplexAgentParams(ComplexEnvironmentParams env) {
		initialAgents = 20;
		mutationRate = 0.05f;
		initEnergy = 100;
		foodEnergy = 100;
		otherFoodEnergy = 25;
		breedEnergy = 60;
		pregnancyPeriod = 0;
		stepEnergy = 1;
		stepRockEnergy = 1;
		turnRightEnergy = 1;
		turnLeftEnergy = 1;
		memoryBits = 2;
		commSimMin = 0;
		stepAgentEnergy = 1;
		communicationBits = 2;
		sexualPregnancyPeriod = 5;
		breedSimMin = 0.0f;
		sexualBreedChance = 1.0f;
		asexualBreedChance = 0.0f;
		wasteInit = 100;
		wasteLimitLoss = 100;
		wasteLimitGain = 100;
		wasteDecay = 0.5f;

		type = -1;
		foodweb = new FoodwebParams(env);
	}
}