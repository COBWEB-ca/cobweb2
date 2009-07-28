package cwcore.complexParams;

import cwcore.ComplexAgent;

public interface ContactMutator extends AgentMutator {
	public void onContact(ComplexAgent bumper, ComplexAgent bumpee);
}
