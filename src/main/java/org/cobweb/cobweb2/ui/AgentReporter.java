package org.cobweb.cobweb2.ui;

import java.io.PrintWriter;
import java.io.Writer;

import org.cobweb.cobweb2.Simulation;
import org.cobweb.cobweb2.core.ComplexAgentStatistics;


public class AgentReporter {

	public static void report(Writer w, Simulation simulation) {
		java.io.PrintWriter pw = new java.io.PrintWriter(w, false);

		printAgentHeaders(pw);

		for (ComplexAgentStatistics info : simulation.theEnvironment.agentInfoVector) {
			printInfo(pw, info);
		}
		pw.flush();
	}


	private static void printAgentHeaders(PrintWriter pw) {
		// Concatenating the headers of the report file.
		pw.print("Agent Number");
		pw.print("\tAgent Type");
		pw.print("\tBirth");
		pw.print("\tParent1\tParent2");
		pw.print("\tDeath");
		pw.print("\tChildren");
		pw.print("\tSexual Pregnancies");

		pw.print("\tSteps");
		pw.print("\tTurns");
		pw.print("\tAgent Bumps");
		pw.print("\tRock Bumps");

		pw.print("\tPD Reward");
		pw.print("\tPD Temptation");
		pw.print("\tPD Sucker");
		pw.print("\tPD Punishment");

		pw.print("\tboundNorth");
		pw.print("\tboundEast");
		pw.print("\tboundSouth");
		pw.print("\tboundWest");

		pw.print("\tEnergy Gained My Food");
		pw.print("\tEnergy Gained Other Food");
		pw.print("\tEnergy Gained Agents");

		pw.print("\tEnergy Lost Movement");
		pw.print("\tEnergy Lost Reproduction");

		pw.println();
	}

	private static void printInfo(PrintWriter pw, ComplexAgentStatistics agentInfo) {
		pw.print(agentInfo.id);
		pw.print("\t" + (agentInfo.type + 1));
		pw.print("\t" + agentInfo.birthTick);
		pw.print("\t" + agentInfo.parent1);
		pw.print("\t" + agentInfo.parent2);
		pw.print("\t" + agentInfo.deathTick);
		pw.print("\t" + agentInfo.directChildren);
		pw.print("\t" + agentInfo.sexualPregs);

		pw.print("\t" + agentInfo.countSteps);
		pw.print("\t" + agentInfo.countTurns);
		pw.print("\t" + agentInfo.countAgentBumps);
		pw.print("\t" + agentInfo.countRockBumps);

		pw.print("\t" + agentInfo.pdReward);
		pw.print("\t" + agentInfo.pdTemptation);
		pw.print("\t" + agentInfo.pdSucker);
		pw.print("\t" + agentInfo.pdPunishment);

		pw.print("\t" + agentInfo.boundNorth);
		pw.print("\t" + agentInfo.boundEast);
		pw.print("\t" + agentInfo.boundSouth);
		pw.print("\t" + agentInfo.boundWest);

		pw.print("\t" + agentInfo.energyGainFoodMine);
		pw.print("\t" + agentInfo.energyGainFoodOther);
		pw.print("\t" + agentInfo.energyGainFoodAgents);

		pw.print("\t" + agentInfo.energyLossMovement);
		pw.print("\t" + agentInfo.energyLossReproduction);

		pw.println();
	}


}
