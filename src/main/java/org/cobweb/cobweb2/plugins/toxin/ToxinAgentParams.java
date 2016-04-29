package org.cobweb.cobweb2.plugins.toxin;

import java.util.Arrays;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.cobweb2.plugins.ResizableParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.util.CloneHelper;
import org.cobweb.util.MutatableFloat;


public class ToxinAgentParams implements ResizableParam {

	/**
	 * Initial toxicity of agent
	 */
	@ConfXMLTag("initialToxicity")
	@ConfDisplayName("Initial toxicity")
	public float initialToxicity = 0;

	/**
	 * Threshold at which toxin starts having an effect
	 */
	@ConfXMLTag("toxicityThreshold")
	@ConfDisplayName("Toxin effect threshold")
	public MutatableFloat toxicityThreshold = new MutatableFloat(1.0f);

	/**
	 * Scale factor for toxicity effect.
	 * Effect = max(0, toxicity - toxicityThreshold) * toxicityEffect
	 */
	@ConfXMLTag("toxicityEffect")
	@ConfDisplayName("Toxin effect factor")
	public MutatableFloat toxicityEffect = new MutatableFloat(-1.0f);

	/**
	 * Which parameter is affected by the toxin.
	 */
	@ConfXMLTag("parameter")
	@ConfDisplayName("Parameter")
	public Phenotype param = new NullPhenotype();

	/**
	 * Toxicity of food types
	 */
	@ConfXMLTag("foodToxicity")
	@ConfDisplayName("Toxicity of Food")
	@ConfList(indexName = "food", startAtOne = true)
	public float[] foodToxicity = new float[0]; // default zero

	/**
	 * Fraction of toxin transfered from consuming different agent types
	 */
	@ConfXMLTag("agentToxicityTransfer")
	@ConfDisplayName("Toxicity transfer from Agent")
	@ConfList(indexName = "agent", startAtOne = true)
	public float[] agentToxicityTransfer = new float[0]; // default zero

	/**
	 * Fraction of toxin transfered to children
	 */
	@ConfXMLTag("childToxicityTransfer")
	@ConfDisplayName("Toxicity transfer to child")
	public float childTransfer = 0.1f;

	/**
	 * Toxin fraction purged per time step
	 */
	@ConfXMLTag("toxicityPurgeRate")
	@ConfDisplayName("Toxin purge rate")
	public MutatableFloat purgeRate = new MutatableFloat(0f);

	public ToxinAgentParams(AgentFoodCountable size) {
		resize(size);
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		foodToxicity = Arrays.copyOf(foodToxicity, envParams.getAgentTypes());
		agentToxicityTransfer = Arrays.copyOf(agentToxicityTransfer, envParams.getAgentTypes());
	}

	@Override
	public ToxinAgentParams clone() {
		try {
			ToxinAgentParams copy = (ToxinAgentParams) super.clone();
			CloneHelper.resetMutatable(copy);
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
