package cwcore.complexParams;

import cwcore.ComplexAgent;

public interface SpawnMutator extends AgentMutator {
	public void onSpawn(ComplexAgent agent);

	public void onSpawn(ComplexAgent agent, ComplexAgent parent);

	public void onSpawn(ComplexAgent agent, ComplexAgent parent1, ComplexAgent parent2);

	public void onDeath(ComplexAgent agent);
}
