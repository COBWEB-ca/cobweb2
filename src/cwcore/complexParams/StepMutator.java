package cwcore.complexParams;

import cobweb.Environment.Location;
import cwcore.ComplexAgent;

public interface StepMutator extends AgentMutator {
	public void onStep(ComplexAgent agent, Location to, Location from);
}
