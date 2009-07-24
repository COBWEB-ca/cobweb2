/**
 *
 */
package cwcore;

import cobweb.Controller;
import cobweb.params.CobwebParam;

class SimpleController implements cobweb.Controller {
	/**
	 *
	 */
	private static final long serialVersionUID = -5220765303102095303L;

	public void addClientAgent(cobweb.Agent a) {
	}

	public void removeClientAgent(cobweb.Agent a) {
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


	public void setupFromEnvironment(int memory, int comm, CobwebParam params) {
	}

	public void setupFromParent(Controller parent, float mutationRate) {
	}

	public void setupFromParents(Controller parent1, Controller parent2, float mutationRate) {
	}


	SimpleControllerParams params;

	public CobwebParam getParams() {
		return params;
	}

	public void setParams(CobwebParam params) {
		this.params = (SimpleControllerParams) params;
	}
}