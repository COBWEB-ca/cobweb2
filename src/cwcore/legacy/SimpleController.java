/**
 *
 */
package cwcore.legacy;

import cobweb.Controller;
import cobweb.params.CobwebParam;
import cwcore.ComplexAgent;
import cwcore.ComplexEnvironment;

@Deprecated
class SimpleController implements cobweb.Controller {
	/**
	 *
	 */
	private static final long serialVersionUID = -5220765303102095303L;

	SimpleControllerParams params;

	public void addClientAgent(cobweb.Agent a) {
		// Nothing
	}

	public void controlAgent(cobweb.Agent baseAgent) {
		ComplexAgent theAgent = (ComplexAgent) baseAgent;
		switch ((int) theAgent.look()) {
			case ComplexEnvironment.FLAG_STONE:
				theAgent.turnLeft();
				break;
			case ComplexEnvironment.FLAG_WASTE:
				theAgent.turnRight();
				break;
			case ComplexEnvironment.FLAG_FOOD:
			default:
				theAgent.step();
		}
	}


	public CobwebParam getParams() {
		return params;
	}

	public void removeClientAgent(cobweb.Agent a) {
		// Nothing
	}

	public void setParams(CobwebParam params) {
		this.params = (SimpleControllerParams) params;
	}


	public void setupFromEnvironment(int memory, int comm, CobwebParam params) {
		// Nothing
	}

	public void setupFromParent(Controller parent, float mutationRate) {
		// Nothing
	}

	public void setupFromParents(Controller parent1, Controller parent2, float mutationRate) {
		// Nothing
	}
}