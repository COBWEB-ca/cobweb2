package org.cobweb.cobweb2.plugins.pd;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.Cause;
import org.cobweb.cobweb2.impl.ComplexAgent;
import org.cobweb.cobweb2.plugins.broadcast.CheaterBroadcast;



public class PDState {
	/**
	 * This agent's last play was cheating
	 */
	public boolean pdCheater;

	/**
	 * This agent's opponents last action was cheating
	 */
	public boolean lastPDcheated;

	private PDMutator mutator;

	private final ComplexAgent agent;

	final PDAgentParams agentParams;

	public PDState(PDMutator mutator, ComplexAgent agent, PDAgentParams pdAgentParams) {
		this.mutator = mutator;
		this.agent = agent;
		this.agentParams = pdAgentParams;
	}

	public void onStepBumpAgent(ComplexAgent adjacentAgent) {
		if (agent.isAgentGood(adjacentAgent) && adjacentAgent.isAgentGood(agent)) {
			playPDonStep(adjacentAgent);
		}
	}

	/**
	 * This method initializes the agents actions in an iterated prisoner's
	 * dilemma game.  The agent can use the following strategies described
	 * by the agentPDStrategy integer:
	 *
	 * <p>0. Default
	 *
	 * <p>The agents decision to defect or cooperate is chosen randomly.
	 * The probability of choosing either is determined by the agents
	 * pdCoopProb parameter.
	 *
	 * <p>1. Tit for Tat
	 *
	 * <p>The agent will initially begin with a cooperate, but will then choose
	 * whatever the opposing agent chose last.  For example, the agent begins
	 * with a cooperate, but if the opposing agent has chosen to defect, then
	 * the agent will choose to defect next round.
	 *
	 */
	public void playPD(Agent other) {

		double coopProb = agentParams.pdCoopProb / 100.0d;

		float similarity = mutator.sim.getSimilarityCalculator().similarity(agent, other);

		coopProb += (similarity - agentParams.pdSimilarityNeutral) * agentParams.pdSimilaritySlope;

		if (agentParams.pdTitForTat) { // if true then agent is playing TitForTat
			pdCheater = lastPDcheated;
		} else {
			pdCheater = false; // agent is assumed to cooperate
			float rnd = mutator.sim.getRandom().nextFloat();
			if (rnd > coopProb)
				pdCheater = true; // agent defects depending on
			// probability
		}

		return;
	}


	/**
	 *Prisoner's dilemma is played between the two agents using the strategies
	 *assigned in playPD().  The agent will use its PD memory to remember agents
	 *that cheat it, which will affect whether an agent will want to meet another,
	 *and its credibility.
	 *
	 *<p>How Prisoner's Dilemma is played:
	 *
	 *<p>Prisoner's dilemma is a game between two agents when they come in to
	 *contact with each other.  The game determines how much energy each agent
	 *receives after contact.  Each agent has two options: cooperate or defect.
	 *The agents choice to cooperate or defect is determined by the strategy the
	 *agent is using (see playPD() method).  The agents choices can lead to
	 *one of four outcomes:
	 *
	 *<p> 1. REWARD for mutual cooperation (Both agents cooperate)
	 *
	 *<p> 2. SUCKER's payoff (Opposing agent defects; this agent cooperates)
	 *
	 *<p> 3. TEMPTATION to defect (Opposing agent cooperates; this agent defects)
	 *
	 *<p> 4. PUNISHMENT for mutual defection (Both agents defect)
	 *
	 *<p>The best strategy for both agents is to cooperate.  However, if an agent
	 *chooses to defect when the other cooperates, the defecting agent will have
	 *a greater advantage.  For a true game of PD, the energy scores for each
	 *outcome should follow this rule: TEMPTATION > REWARD > PUNISHMENT > SUCKER
	 *
	 *<p>Here is an example of how much energy an agent could receive:
	 *<br> REWARD     =>     5
	 *<br> SUCKER     =>     2
	 *<br> TEMPTATION =>     8
	 *<br> PUNISHMENT =>     3
	 *
	 * @param adjacentAgent Agent playing PD with
	 * @param othersID ID of the adjacent agent.
	 * @see ComplexAgent#playPD(ComplexAgent)
	 * @see <a href="http://en.wikipedia.org/wiki/Prisoner's_dilemma">Prisoner's Dilemma</a>
	 */
	@SuppressWarnings("javadoc")
	public void playPDonStep(Agent adjacentAgent) {
		PDState otherState = mutator.getAgentState(adjacentAgent);

		playPD(adjacentAgent);
		otherState.playPD(this.agent);

		// Save result for future strategy (tit-for-tat, learning, etc.)
		lastPDcheated = otherState.pdCheater;
		otherState.lastPDcheated = pdCheater;

		/*
		 * TODO LOW: The ability for the PD game to contend for the Get the food tiles immediately around each agents
		 */

		if (!pdCheater && !otherState.pdCheater) {
			/* Both cooperate */
			agent.changeEnergy(+mutator.params.reward, new PDRewardCause());
			adjacentAgent.changeEnergy(+mutator.params.reward, new PDRewardCause());

		} else if (!pdCheater && otherState.pdCheater) {
			/* Only other agent cheats */
			agent.changeEnergy(+mutator.params.sucker, new PDSuckerCause());
			adjacentAgent.changeEnergy(+mutator.params.temptation, new PDTemptationCause());

		} else if (pdCheater && !otherState.pdCheater) {
			/* Only this agent cheats */
			agent.changeEnergy(+mutator.params.temptation, new PDTemptationCause());
			adjacentAgent.changeEnergy(+mutator.params.sucker, new PDSuckerCause());

		} else if (pdCheater && otherState.pdCheater) {
			/* Both cheat */
			agent.changeEnergy(+mutator.params.punishment, new PDPunishmentCause());
			adjacentAgent.changeEnergy(+mutator.params.punishment, new PDPunishmentCause());
		}

		if (otherState.pdCheater)
			iveBeenCheated(adjacentAgent);
	}

	/**
	 * The agent will remember the last variable number of agents that
	 * cheated it.  How many cheaters it remembers is determined by its
	 * PD memory size.
	 */
	private void iveBeenCheated(Agent cheater) {

		ComplexAgent a = agent;
		a.rememberBadAgent((ComplexAgent)cheater);

		broadcastCheating((ComplexAgent)cheater);
	}

	private void broadcastCheating(ComplexAgent cheater) {
		agent.broadcast(new CheaterBroadcast(cheater, agent));
	}


	public static abstract class PDCause implements Cause {
		@Override
		public String getName() { return "PD"; }
	}
	public static class PDRewardCause extends PDCause {
		@Override
		public String getName() { return "PD Reward"; }
	}
	public static class PDTemptationCause extends PDCause {
		@Override
		public String getName() { return "PD Temptation"; }
	}
	public static class PDSuckerCause extends PDCause {
		@Override
		public String getName() { return "PD Sucker"; }
	}
	public static class PDPunishmentCause extends PDCause {
		@Override
		public String getName() { return "PD Punishment"; }
	}


}

