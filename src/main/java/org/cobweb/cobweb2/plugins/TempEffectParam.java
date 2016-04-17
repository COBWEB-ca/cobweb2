package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.NullPhenotype;
import org.cobweb.cobweb2.core.Phenotype;
import org.cobweb.io.ConfDisplayName;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;
import org.cobweb.util.CloneHelper;
import org.cobweb.util.MutatableFloat;
import org.cobweb.util.MutatableInt;


public class TempEffectParam implements ParameterSerializable {

	@ConfXMLTag("parameter")
	@ConfDisplayName("Parameter")
	public Phenotype parameter = new NullPhenotype();

	@ConfXMLTag("factor")
	@ConfDisplayName("Factor")
	public MutatableFloat factor = new MutatableFloat(1.1f);

	@ConfXMLTag("duration")
	@ConfDisplayName("Duration")
	public MutatableInt duration = new MutatableInt(100);

	@Override
	public TempEffectParam clone() {
		try {
			TempEffectParam copy = (TempEffectParam) super.clone();
			CloneHelper.resetMutatable(copy);
			return copy;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final long serialVersionUID = 1L;
}
