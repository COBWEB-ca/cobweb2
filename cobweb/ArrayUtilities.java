package cobweb;

import java.lang.reflect.Array;

public class ArrayUtilities {

	/**
	 * initArray takes an array given as an argument, an array of dimensions, and counter indicated the what dimension
	 * in the array of dimensions should be used first.
	 *
	 * It returns a possibly new array that has the dimensions specified by the second parameter and all of the old data
	 * from the array given as a parameter (that fits).
	 *
	 * Essentially, it is a utility for resizing arrays
	 *
	 * @param <T> Array type
	 */

	public static <T> T resizeArray(T original, int... newsize) {
//		if (original == null) {
//			return null;
//		}
		if (!original.getClass().isArray()) {
			return original;
		}

		int originalLen = Array.getLength(original);
		int newLen = newsize[0];
		T result = original;
		Class<?> innerType = original.getClass().getComponentType();

		if (originalLen != newLen) {
			@SuppressWarnings("unchecked")
			T uncheckedResult = (T) Array.newInstance(innerType, newLen);
			result = uncheckedResult;
			System.arraycopy(original, 0, result, 0, Math.min(originalLen, newLen));
		}

		if (innerType.isArray()) {
			for (int i = 0; i < Array.getLength(result); ++i) {
				if (Array.get(result, i) == null) {
					Array.set(result, i, Array.newInstance(innerType.getComponentType(), newsize[1]));
				}
				int[] otherSizes = new int[newsize.length - 1];
				System.arraycopy(newsize, 1, otherSizes, 0, otherSizes.length);
				Object temp = resizeArray(Array.get(result, i), otherSizes);
				Array.set(result, i, temp);
			}
		}

		return result;
	}

}