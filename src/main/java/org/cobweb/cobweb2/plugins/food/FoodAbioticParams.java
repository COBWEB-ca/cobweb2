package org.cobweb.cobweb2.plugins.food;

import java.util.Arrays;

import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfList;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;


public class FoodAbioticParams implements ParameterSerializable {

	@ConfDisplayName("")
	@ConfXMLTag("FactorParams")
	@ConfList(indexName = "Factor", startAtOne = true)
	public FoodFactorParams[] factorParams = new FoodFactorParams[0];

	public void resizeFields(int fieldCount) {
		FoodFactorParams[] n = Arrays.copyOf(factorParams, fieldCount);
		for (int i = factorParams.length; i < n.length; i++) {
			n[i] = new FoodFactorParams();
		}
		factorParams = n;
	}

	private static final long serialVersionUID = 1L;
}
