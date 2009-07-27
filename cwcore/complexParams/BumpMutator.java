package cwcore.complexParams;

import cwcore.ComplexAgent;

public interface BumpMutator extends AgentMutator {
	public void bump(ComplexAgent bumper, ComplexAgent bumpee);
}
