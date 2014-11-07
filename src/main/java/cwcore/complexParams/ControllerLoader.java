/**
 *
 */
package cwcore.complexParams;

import cobweb.params.CobwebParam;
import cobweb.params.DynamicConfInstantiator;

/**
 * Loads a controller configuration for the given controller
 */
public class ControllerLoader implements DynamicConfInstantiator {

	public CobwebParam instantiate(CobwebParam parentConf) {
		ComplexEnvironmentParams ep = (ComplexEnvironmentParams) parentConf;
		CobwebParam p;
		try {
			// Controller of class X has its parameters in class XParams
			p = (CobwebParam) Class.forName(ep.controllerName + "Params").newInstance();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Could not load controller", ex);
		}
		return p;
	}
}