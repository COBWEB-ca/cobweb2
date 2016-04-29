package org.cobweb.cobweb2.plugins.genetics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.cobweb.cobweb2.core.Phenotype;

public class PhenotypeIndexTest extends TestCase{

	public void testPossiblePhenotypes() {
		Set<Phenotype> actual = PhenotypeIndex.getPossibleValues();
		List<String> expected = new ArrayList<String>(Arrays.asList(new String[] {
				"None",
				"FoodEnergy",
				"OtherFoodEnergy",
				"AgentFoodEnergyFraction",
				"BreedEnergy",
				"pregnancyPeriod",
				"InitEnergy",
				"StepEnergy",
				"StepRockEnergy",
				"StepAgentEnergy",
				"TurnRightEnergy",
				"TurnLeftEnergy",
				"MutationRate",
				"commSimMin",
				"sexualBreedChance",
				"asexualBreedChance",
				"breedSimMin",
				"sexualPregnancyPeriod",
				"agingLimit",
				"agingRate",
				"broadcastFixedRange",
				"broadcastEnergyMin",
				"broadcastEnergyCost",
				"broadcastMinSimilarity",
				"pdCoopProb",
				"pdSimilaritySlope",
				"pdSimilarityNeutral",
				"productionCost",
				"productPrice",
				"maxUnsold",
				"productExpiry",
				"productEffect.factor",
				"productEffect.duration",
				"wasteConsumptionEnergy"
		}));

		for (Phenotype phenotype : actual) {
			final String id = phenotype.getIdentifier();
			assertTrue("Unexpected phenotype: " + id, expected.remove(id));
		}

		for (String id : expected) {
			fail("Missing phenotype: " + id);
		}
	}


	public void testUniquePhenotypes() {
		Set<Phenotype> actual = PhenotypeIndex.getPossibleValues();
		Set<String> ids = new HashSet<>();
		for (Phenotype phenotype : actual) {
			if (!ids.add(phenotype.getIdentifier())) {
				fail("Duplicate phenotype identifiers: " + phenotype.getIdentifier());
			}
		}
	}

}
