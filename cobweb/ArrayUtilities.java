package cobweb;


public class ArrayUtilities {

	/**
	 * initArray takes an array given as an argument, an array of dimensions,
	 * and counter indicated the what dimension in the array of dimensions
	 * should be used first.
	 * 
	 * It returns a possibly new array that has the dimensions specified by the
	 * second parameter and all of the old data from the array given as a
	 * parameter (that fits).
	 * 
	 * Essentially, it is a utility for resizing arrays
	 */

	public static Object initArray(Object targetArray, int[] indices,
			int iElement) {

		if (iElement >= indices.length)
			return targetArray;

		Object tmp = targetArray;
		if (java.lang.reflect.Array.getLength(targetArray) != indices[iElement]) {

			targetArray = java.lang.reflect.Array.newInstance(targetArray
					.getClass().getComponentType(), indices[iElement]);

			if (targetArray.getClass().getComponentType().equals(int.class)) {
				for (int i = 0; i < java.lang.reflect.Array
						.getLength(targetArray)
						&& i < java.lang.reflect.Array.getLength(tmp); ++i) {
					java.lang.reflect.Array.setInt(targetArray, i,
							java.lang.reflect.Array.getInt(tmp, i));
				}
			} else if (targetArray.getClass().getComponentType().equals(
					long.class)) {
				for (int i = 0; i < java.lang.reflect.Array
						.getLength(targetArray)
						&& i < java.lang.reflect.Array.getLength(tmp); ++i) {
					java.lang.reflect.Array.setLong(targetArray, i,
							java.lang.reflect.Array.getLong(tmp, i));
				}
			} else if (targetArray.getClass().getComponentType().equals(
					float.class)) {
				for (int i = 0; i < java.lang.reflect.Array
						.getLength(targetArray)
						&& i < java.lang.reflect.Array.getLength(tmp); ++i) {
					java.lang.reflect.Array.setFloat(targetArray, i,
							java.lang.reflect.Array.getFloat(tmp, i));
				}
			} else if (targetArray.getClass().getComponentType().equals(
					boolean.class)) {
				for (int i = 0; i < java.lang.reflect.Array
						.getLength(targetArray)
						&& i < java.lang.reflect.Array.getLength(tmp); ++i) {
					java.lang.reflect.Array.setBoolean(targetArray, i,
							java.lang.reflect.Array.getBoolean(tmp, i));
				}
			} else { // its a non-primitive
				for (int i = 0; i < java.lang.reflect.Array
						.getLength(targetArray)
						&& i < java.lang.reflect.Array.getLength(tmp); ++i) {
					java.lang.reflect.Array.set(targetArray, i,
							java.lang.reflect.Array.get(tmp, i));
				}
			}
		}

		if (targetArray.getClass().getComponentType().isArray()) {

			Object[] theDest = (Object[]) targetArray;

			for (int i = 0; i < theDest.length; ++i) {

				if (theDest[i] == null) {
					theDest[i] = java.lang.reflect.Array.newInstance(theDest
							.getClass().getComponentType().getComponentType(),
							indices[iElement + 1]);
				}// else
				theDest[i] = initArray(theDest[i], indices, iElement + 1);
			}
		}

		return targetArray;
	}
}