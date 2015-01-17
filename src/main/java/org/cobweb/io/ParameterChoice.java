package org.cobweb.io;


/**
 * Parameter value that is one of multiple choices available
 */
public interface ParameterChoice {

	/**
	 * Unique Identifier for this choice.
	 * This identifier is used to serialize and de-serialize the parameter.
	 * @return identifier
	 */
	public String getIdentifier();

	/**
	 * User-friendly name of the choice.
	 * @return user-friendly name
	 */
	public String getName();
}
