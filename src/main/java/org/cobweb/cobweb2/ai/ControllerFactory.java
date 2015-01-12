package org.cobweb.cobweb2.ai;

import java.lang.reflect.InvocationTargetException;

import org.cobweb.cobweb2.core.StatePluginSource;
import org.cobweb.cobweb2.core.SimulationInternals;
import org.cobweb.cobweb2.io.CobwebParam;

public class ControllerFactory {

	private Class<? extends Controller> controllerClass;

	private CobwebParam params;

	private StatePluginSource simulation;


	public ControllerFactory(String controllerName, CobwebParam cobwebParams, StatePluginSource simulation) throws ClassNotFoundException  {
		this.simulation = simulation;
		@SuppressWarnings("unchecked")
		Class<? extends Controller> c = (Class<? extends Controller>) Class.forName(controllerName);
		controllerClass = c;
		params = cobwebParams;
	}

	public Controller createNew(int memory, int comm, int type) {
		Controller c = newInstance();
		c.setupFromEnvironment(memory, comm, params, type);
		return c;
	}

	private Controller newInstance() {
		try {
			Controller c = controllerClass.getConstructor(SimulationInternals.class).newInstance(simulation);
			return c;
		} catch (InstantiationException ex) {
			throw new RuntimeException("Unable to instantiate controller", ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("Unable to instantiate controller", ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException("Unable to instantiate controller", ex);
		} catch (SecurityException ex) {
			throw new RuntimeException("Unable to instantiate controller", ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException("Unable to instantiate controller", ex);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("Unable to instantiate controller", ex);
		}
	}

}
