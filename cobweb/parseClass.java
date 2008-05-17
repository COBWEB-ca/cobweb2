package cobweb;


public class parseClass {

	/**
	 * parseLoad takes 3 parameters, a reader to use an input stream, a special
	 * string indicate the end of the input, and a hashtable containing 1
	 * dimensional arrays that can be arrays of anything (including arrays)
	 * using strings as keys.
	 * 
	 * The file is scanned for these keys, and for each one, parseLoad
	 * determines what type of data is associated with that name and attemps to
	 * read the next token of input as a value for that parameter. Unrecognized
	 * strings or parameters not given the proper type of values will throw
	 * exceptions.
	 * 
	 * The special parameter is "Index" which can be used to specify what array
	 * indices, if any, are used when the subsequent parameter are arrays (which
	 * is the case with all agent-type dependant parameters).
	 * 
	 * 
	 */
	public static void parseLoad(java.io.Reader r, String endString,
			java.util.Hashtable<String, Object> parseData) throws java.io.IOException,
			ArrayIndexOutOfBoundsException {

		java.io.StreamTokenizer inTokens = new java.io.StreamTokenizer(r);

		int[] indexStore = new int[256];
		for (int i = 0; i < 256; ++i)
			indexStore[i] = 0;

		try {
			while (true) {
				if (inTokens.nextToken() != java.io.StreamTokenizer.TT_WORD)
					throw new java.io.IOException();

				if (inTokens.sval.equalsIgnoreCase("Index")) {
					for (int j = 0; inTokens.nextToken() == java.io.StreamTokenizer.TT_NUMBER
							&& j < 256; ++j) {
						indexStore[j] = (int) inTokens.nval;
					}
				}

				String varName = inTokens.sval;

				Object theDest;
				if ((theDest = parseData.get(inTokens.sval.toLowerCase())) != null) {
					int idx = 0;

					for (int j = 0; theDest.getClass().getComponentType()
							.isArray()
							&& j < 256; ++j) {
						theDest = java.lang.reflect.Array.get(theDest, idx);
						idx = indexStore[j];
					}

					if (theDest.getClass().getComponentType().equals(int.class)) {
						if (inTokens.nextToken() != java.io.StreamTokenizer.TT_NUMBER)
							throw new java.io.IOException(
									"Expected number for " + varName);
						java.lang.reflect.Array.setInt(theDest, idx,
								(int) inTokens.nval);
					} else if (theDest.getClass().getComponentType().equals(
							long.class)) {
						if (inTokens.nextToken() != java.io.StreamTokenizer.TT_NUMBER)
							throw new java.io.IOException(
									"Expected number for " + varName);
						java.lang.reflect.Array.setLong(theDest, idx,
								(long) inTokens.nval);
					} else if (theDest.getClass().getComponentType().equals(
							float.class)) {
						if (inTokens.nextToken() != java.io.StreamTokenizer.TT_NUMBER)
							throw new java.io.IOException(
									"Expected number for " + varName);
						java.lang.reflect.Array.setFloat(theDest, idx,
								(float) inTokens.nval);
					} else if (theDest.getClass().getComponentType().equals(
							boolean.class)) {
						if (inTokens.nextToken() != java.io.StreamTokenizer.TT_WORD)
							throw new java.io.IOException(
									"Expected boolean for " + varName);
						java.lang.reflect.Array.setBoolean(theDest, idx,
								Boolean.valueOf(inTokens.sval).booleanValue());
					} else {

						Object[] arrayDest = (Object[]) theDest;

						java.lang.reflect.Constructor<?>[] theCtors = arrayDest
								.getClass().getComponentType()
								.getConstructors();
						java.lang.reflect.Constructor<?> theCtor = null;

						for (int j = 0; j < theCtors.length; ++j) {
							Class<?>[] params = theCtors[j].getParameterTypes();
							if (params != null && params.length == 1
									&& params[0].equals(String.class)) {
								theCtor = theCtors[j];
								break;
							}
						}
						if (theCtor == null)
							throw new InstantiationError(
									"No valid constructor found on the "
											+ arrayDest[0].getClass().getName()
											+ " class.");
						else {
							// the following code is a little hackish because
							// java.io.StreamTokenizer was badly written (when
							// in doubt, blame someone else)
							if (inTokens.nextToken() == java.io.StreamTokenizer.TT_WORD)
								arrayDest[idx] = theCtor
										.newInstance(new Object[] { inTokens.sval });
							else if (theDest.getClass().getComponentType()
									.equals(Integer.class))
								arrayDest[idx] = theCtor
										.newInstance(new Object[] { Integer
												.toString((int) inTokens.nval) });
							else
								arrayDest[idx] = theCtor
										.newInstance(new Object[] { Double
												.toString(inTokens.nval) });
						}

					}
				} else if (inTokens.sval != null
						&& inTokens.sval.equalsIgnoreCase(endString)) {
					return;
				} else if (theDest == null) {
					throw new InstantiationError(inTokens.sval
							+ " is not a recognized parameter.");
				}

			} // end while(true)

		} catch (SecurityException e) {
			throw new InstantiationError(e.toString());
		} catch (InstantiationException e) {
			throw new InstantiationError(e.toString());
		} catch (IllegalAccessException e) {
			throw new InstantiationError(e.toString());
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw new InstantiationError(e.getTargetException().toString());
		} catch (java.io.IOException e) {
			throw new InstantiationError(e.toString());
		}

	}

	/**
	 * parseSave takes 5 parameters. A PrintWriter to output to, an array of
	 * objects that contains values to be saved, an array of strings that
	 * contain the desired parameter names (should be the same size as the array
	 * of value), an array of indices to use assuming the given objects are all
	 * arrays, and finally the number of indices to use (indicating array
	 * dimension)
	 * 
	 * It outputs appropriate "Index" changes before parameters that need them,
	 * and tries to output all of data in order of array dimension (a non-array
	 * being considered an array of dimension 0) in order to minimize the number
	 * of times "Index" must be used.
	 * 
	 */
	public static void parseSave(java.io.PrintWriter pw, Object[] saveArray,
			String[] names, int[] indices, int indCount)
			throws java.io.IOException {

		int[] store = new int[saveArray.length];
		int k = 0;

		if (indCount > 0) {
			pw.print("Index");
			for (int i = 0; i < indCount; ++i)
				pw.print(" " + indices[i]);
			pw.println("");
			pw.println("");
		}

		for (int i = 0; i < saveArray.length; i++) {

			if (saveArray[i].getClass().isArray())
				store[k++] = i;
			else
				pw.println(names[i] + " " + saveArray[i]);
		}
		pw.println("");

		for (int i = 0;; ++i) {

			int l = 0;

			for (int j = 0; j < k; ++j)
				if (java.lang.reflect.Array.getLength(saveArray[store[j]]) > i)
					store[l++] = store[j];

			if ((k = l) == 0)
				break;

			Object[] saveArrayElements = new Object[l];
			String[] nameElements = new String[l];

			for (int j = 0; j < l; ++j) {
				saveArrayElements[j] = java.lang.reflect.Array.get(
						saveArray[store[j]], i);
				nameElements[j] = names[store[j]];
			}
			indices[indCount] = i;
			pw.println("");
			parseSave(pw, saveArrayElements, nameElements, indices,
					indCount + 1);

		}

	}
}