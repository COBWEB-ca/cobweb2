package org.cobweb.util;

import java.lang.reflect.Field;

public class ReflectionUtil {

	/**
	 * Modifies value of <code>object.field</code> using the formula
	 * <code>x' = x * m + b</code>
	 *
	 * @param object Object to be modified
	 * @param field Field to be modified
	 * @param m scale factor
	 * @param b offset factor
	 */
	public static void modifyFieldLinear(Object object, Field field, float m, float b) {
		try {
			Object o;
			o = field.get(object);

			// Modify the value according to the coefficient.
			if (o instanceof Float) {
				float value = ((Float) o).floatValue();
				field.setFloat(object, value * m + b);
			} else if (o instanceof Integer) {
				double value = ((Integer) o).doubleValue();
				field.setInt(object, (int) Math.round(value * m + b));
			} else {
				throw new IllegalArgumentException("Unknown phenotype field type");
			}
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("Cannot access field: " + field.toString(), ex);
		}
	}


	@SuppressWarnings("boxing")
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
