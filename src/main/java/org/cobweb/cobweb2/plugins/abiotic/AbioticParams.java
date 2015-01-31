package org.cobweb.cobweb2.plugins.abiotic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cobweb.cobweb2.core.AgentFoodCountable;
import org.cobweb.cobweb2.core.StatePluginSource;
import org.cobweb.cobweb2.plugins.PerAgentParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfListType;
import org.cobweb.io.ConfXMLTag;

public class AbioticParams extends PerAgentParams<AbioticAgentParams> implements StatePluginSource {

	@ConfDisplayName("Factor")
	@ConfXMLTag("Factors")
	@ConfList(indexName = "Factor", startAtOne = true)
	@ConfListType(AbioticFactor.class)
	public List<AbioticFactor> factors = new ArrayList<AbioticFactor>();

	public AbioticParams(AgentFoodCountable size) {
		super(AbioticAgentParams.class);
		VerticalBands verticalBands = new VerticalBands();
		verticalBands.bands.add(0f);
		verticalBands.bands.add(1f);
		verticalBands.bands.add(2f);
		verticalBands.bands.add(0f);
		factors.add(verticalBands);
		HorizontalBands hBands = new HorizontalBands();
		hBands.bands.add(0f);
		hBands.bands.add(10f);
		factors.add(hBands);
		resize(size);
	}

	@Override
	protected AbioticAgentParams newAgentParam() {
		return new AbioticAgentParams();
	}

	@Override
	public void resize(AgentFoodCountable envParams) {
		super.resize(envParams);
		for (int i = 0; i < agentParams.length; i++) {
			agentParams[i].resizeFields(factors.size());
		}
	}


	static final String STATE_NAME_ABIOTIC_PENALTY = "Abiotic %d Penalty";
	@Override
	public Collection<String> getStatePluginKeys() {
		Collection<String> result = new ArrayList<>();
		for (int i = 0; i < factors.size(); i++) {
			result.add(String.format(STATE_NAME_ABIOTIC_PENALTY, i + 1));
		}
		return result;
	}

	private static final long serialVersionUID = 2L;
}
