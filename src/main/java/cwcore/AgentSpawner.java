package cwcore;

import java.lang.reflect.InvocationTargetException;

import cobweb.Agent;


public class AgentSpawner {

	private static Class<?> spawnType;

	public static void SetType(String classname) {
		try {
			spawnType = Class.forName(classname);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Agent spawn() {
		try {
			return (Agent)spawnType.getConstructor((Class<?>[])null).newInstance((Object[])null);

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
