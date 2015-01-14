package org.cobweb.cobweb2.ai;

import java.util.LinkedHashMap;
import java.util.Map;

import org.cobweb.cobweb2.core.params.SimulationParams;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfMap;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

public class GeneticStateAgentParams implements ParameterSerializable {

	private static final long serialVersionUID = -6295295048720208502L;

	@ConfDisplayName("state")
	@ConfXMLTag("StateSize")
	@ConfMap(entryName = "State", keyName = "Name", valueClass = int.class)
	public Map<String, Integer> stateSizes = new LinkedHashMap<String, Integer>();

	public GeneticStateAgentParams(SimulationParams simParam) {
		for(String p : simParam.getPluginParameters()) {
			stateSizes.put(p, 0);
		}
	}

}
