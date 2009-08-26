/**
 *
 */
package cwcore.complexParams;

import cobweb.params.CobwebParam;
import cobweb.params.DynamicConfInstantiator;

public class ControllerLoader implements DynamicConfInstantiator {
	public CobwebParam instantiate(CobwebParam parentConf) {
		ComplexEnvironmentParams ep = (ComplexEnvironmentParams) parentConf;
		CobwebParam p;
		try {
			p = (CobwebParam) Class.forName(ep.controllerName + "Params").newInstance();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Could not load controller", ex);
		}
		return p;
	}
}