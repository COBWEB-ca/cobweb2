package org.cobweb.cobweb2.io;

import java.lang.reflect.Field;


/**
 * A plugin parameter. To be adjustable from the UI it needs a user friendly name
 *
 */
public interface NamedParam {

	/**
	 * Get user friendly name for parameter
	 * @return user friendly name for parameter
	 */
	public String getName();

	public Field getField();

}
