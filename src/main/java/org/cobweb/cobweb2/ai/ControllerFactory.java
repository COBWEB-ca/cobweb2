package org.cobweb.cobweb2.ai;

import java.lang.reflect.InvocationTargetException;

import org.cobweb.cobweb2.core.SimulationInterface;
import org.cobweb.cobweb2.io.CobwebParam;

public class ControllerFactory {

	private Class<? extends Controller> controllerClass;

	private CobwebParam params;

	private SimulationInterface simulation;


	public ControllerFactory(String controllerName, CobwebParam cobwebParams, SimulationInterface simulation) throws ClassNotFoundException  {
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
			Controller c = controllerClass.getConstructor(SimulationInterface.class).newInstance(simulation);
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

	public Controller createFromParent(Controller p, float mutation) {
		Controller c = newInstance();
		c.setupFromParent(p, mutation);
		return c;
	}

	public Controller createFromParents(Controller p1, Controller p2, float mutation) {
		Controller c = newInstance();
		c.setupFromParents(p1, p2, mutation);
		return c;
	}

}
