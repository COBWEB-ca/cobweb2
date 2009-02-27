/**
 * 
 */
package cwcore;

@SuppressWarnings("unused") class SimpleController implements cobweb.Controller {
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
}