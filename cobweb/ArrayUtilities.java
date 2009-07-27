package cobweb;

import java.lang.reflect.Array;

public class ArrayUtilities {

	/**
	 * Resizes a given array to the given dimensions.
	 * If a dimension is already the right size, it is re-used, otherwise a new array is created
	 *
	 * @param original original array
	 * @param newsize new dimensions of the array
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