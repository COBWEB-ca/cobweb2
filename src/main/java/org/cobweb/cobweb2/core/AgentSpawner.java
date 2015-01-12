package org.cobweb.cobweb2.core;

import java.lang.reflect.InvocationTargetException;


public class AgentSpawner {

	private Class<?> spawnType;
	private StatePluginSource simulation;

	public AgentSpawner(String classname, StatePluginSource sim) {
		simulation = sim;
		try {
			spawnType = Class.forName(classname);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	public ComplexAgent spawn() {
		try {
			return (ComplexAgent)spawnType.getConstructor(SimulationInternals.class).newInstance(simulation);

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
