package cwcore.complexParams;

import cwcore.ComplexAgent;

public interface ContactMutator extends AgentMutator {
	public void bump(ComplexAgent bumper, ComplexAgent bumpee);
}
