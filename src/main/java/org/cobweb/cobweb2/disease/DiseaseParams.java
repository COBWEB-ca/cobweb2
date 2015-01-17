package org.cobweb.cobweb2.disease;

import java.util.Arrays;

import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.core.params.AgentFoodCountable;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

public class DiseaseParams implements ParameterSerializable {

	private static final long serialVersionUID = 6866958975246266955L;

	@ConfXMLTag("Index")
	public int type;

	/**
	 * Fraction of initially infected agents.
	 */
	@ConfXMLTag("initialInfection")
	@ConfDisplayName("Initially infected fraction")
	public float initialInfection = 0;

	/**
	 * Chance this agent will get a disease from contact with an infected agent.
	 */
	@ConfXMLTag("contactTransmitRate")
	@ConfDisplayName("Contact transmission rate")
	public float contactTransmitRate = 0.5f;

	/**
	 * Chance a child of an infected agent will be infected.
	 */
	@ConfXMLTag("childTransmitRate")
	@ConfDisplayName("Child transmission rate")
	public float childTransmitRate = 0.9f;

	/**
	 * Which parameter is affected by the disease.
	 */
	@ConfXMLTag("parameter")
	@ConfDisplayName("Parameter")
	public Phenotype param = new NullPhenotype();

	/**
	 * The factor the parameter is multiplied by when the agent is infected.
	 */
	@ConfXMLTag("factor")
	@ConfDisplayName("Factor")
	public float factor = 2;


	@ConfXMLTag("vaccinator")
	@ConfDisplayName("Vaccinator")
	public boolean vaccinator = false;

	@ConfXMLTag("vaccineEffectiveness")
	@ConfDisplayName("Vaccine Effectiveness")
	public float vaccineEffectiveness = 1.0f;

	@ConfXMLTag("healer")
	@ConfDisplayName("Healer")
	public boolean healer = false;

	@ConfXMLTag("healerEffectiveness")
	@ConfDisplayName("Healing Effectiveness")
	public float healerEffectiveness = 1.0f;

	@ConfXMLTag("recoveryTime")
	@ConfDisplayName("Recovery time")
	public int recoveryTime = 0;

	/**
	 * Agent types this agent can transmit the disease to.
	 */
	@ConfDisplayName("Transmit to")
	@ConfXMLTag("transmitTo")
	@ConfList(indexName = "agent", startAtOne = true)
	public boolean[] transmitTo;


	private final transient AgentFoodCountable env;

	public DiseaseParams(AgentFoodCountable env) {
		this.env = env;
		transmitTo = new boolean[env.getAgentTypes()];
	}

	public void resize() {
		boolean[] n = Arrays.copyOf(transmitTo, env.getAgentTypes());
		this.transmitTo = n;
	}

}
