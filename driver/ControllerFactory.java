package driver;

import java.lang.reflect.Constructor;

import cobweb.Controller;

public class ControllerFactory {

	private static Constructor<? extends Controller> emptyConstructor;

	private static Constructor<? extends Controller> asexualConstructor;

	private static Constructor<? extends Controller> sexualConstructor;


	public static void Init(Class<? extends Controller> controllerClass) throws NoSuchMethodException {
		emptyConstructor = controllerClass.getConstructor(int.class, int.class);
		asexualConstructor = controllerClass.getConstructor(Controller.class, float.class);
		sexualConstructor = controllerClass.getConstructor(Controller.class, Controller.class, float.class);
	}

	@SuppressWarnings("unchecked")
	public static void Init(String controllerName) throws ClassNotFoundException  {
		Class<? extends Controller> aiClass;
		aiClass = (Class<? extends Controller>) Class.forName(controllerName);
		try {
			Init(aiClass);
		} catch (NoSuchMethodException ex) {
			throw new ClassNotFoundException(controllerName + " Doesn't support all the constructors");
		}
	}

	public static Controller createNew(int memory, int comm) {
		try {
			return emptyConstructor.newInstance(memory, comm);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Controller createFromParent(Controller p, float mutation) {
		try {
			return asexualConstructor.newInstance(p, mutation);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Controller createFromParents(Controller p1, Controller p2, float mutation) {
		try {
			return sexualConstructor.newInstance(p1, p2, mutation);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
