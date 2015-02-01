package org.cobweb.cobweb2.plugins.food;

import org.cobweb.cobweb2.plugins.abiotic.AbioticPreferenceParam;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfSquishParent;
import org.cobweb.io.ParameterSerializable;


public class FoodFactorParams implements ParameterSerializable {

	@ConfDisplayName("Preference")
	@ConfSquishParent
	public AbioticPreferenceParam preference = new AbioticPreferenceParam();

	private static final long serialVersionUID = 1L;
}
