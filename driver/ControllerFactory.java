package driver;

import cobweb.Controller;
import cobweb.params.CobwebParam;

public class ControllerFactory {

	private static Class<? extends Controller> controllerClass;

	private static CobwebParam params;


	public static void Init(String controllerName, CobwebParam cobwebParams) throws ClassNotFoundException  {
		@SuppressWarnings("unchecked")
		Class<? extends Controller> c = (Class<? extends Controller>) Class.forName(controllerName);
		controllerClass = c;
		params = cobwebParams;
	}

	public static Controller createNew(int memory, int comm) {
		Controller c = newInstance();
		c.setupFromEnvironment(memory, comm, params);
		return c;
	}

	private static Controller newInstance() {
		try {
			Controller c = ControllerFactory.controllerClass.newInstance();
			return c;
		} catch (InstantiationException ex) {
			throw new RuntimeException("Unable to instantiate controller", ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("Unable to instantiate controller", ex);
		}
	}

	public static Controller createFromParent(Controller p, float mutation) {
		Controller c = newInstance();
		c.setupFromParent(p, mutation);
		return c;
	}

	public static Controller createFromParents(Controller p1, Controller p2, float mutation) {
		Controller c = newInstance();
		c.setupFromParents(p1, p2, mutation);
		return c;
	}

}
