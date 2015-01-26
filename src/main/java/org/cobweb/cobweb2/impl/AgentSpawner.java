package org.cobweb.cobweb2.impl;

import java.lang.reflect.InvocationTargetException;

import org.cobweb.cobweb2.core.Agent;
import org.cobweb.cobweb2.core.SimulationInternals;


public class AgentSpawner {

	private Class<?> spawnType;
	private SimulationInternals simulation;

	public AgentSpawner(String classname, SimulationInternals sim) {
		simulation = sim;
		try {
			spawnType = Class.forName(classname);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	public Agent spawn(int type) {
		try {
			return (Agent)spawnType.getConstructor(SimulationInternals.class, int.class).newInstance(simulation, type);

		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}

	}
}
