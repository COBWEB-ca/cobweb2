package org.cobweb.cobweb2.core;

public interface SimulationTimeSpace extends RandomSource {

	public abstract long getTime();

	public abstract Topology getTopology();

}
