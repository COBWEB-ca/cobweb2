package cobweb.params;

public class ReflectionUtil {

	public static final Object stringToBoxed(Class<?> t, String strVal) {
		if (t.equals(boolean.class)) {
			return Boolean.parseBoolean(strVal);
		} else if (t.equals(byte.class)) {
			return Byte.parseByte(strVal);
		} else if (t.equals(char.class)) {
			return strVal.charAt(0);
		} else if (t.equals(double.class)) {
			return Double.parseDouble(strVal);
		} else if (t.equals(float.class)) {
			return Float.parseFloat(strVal);
		} else if (t.equals(int.class)) {
			return Integer.parseInt(strVal);
		} else if (t.equals(long.class)) {
			return Long.parseLong(strVal);
		} else if (t.equals(short.class)) {
			return Short.parseShort(strVal);
		} else if (t.equals(String.class)) {
			return strVal;
		}
		throw new IllegalArgumentException("Can't parse non-primitive type: " + t.getCanonicalName());
	}
}
