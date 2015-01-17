package org.cobweb.io;


/**
 * Parameter value that is one of multiple choices available
 */
public interface ParameterChoice {

	/**
	 * Unique Identifier for this choice of parameter.
	 * This identifier is used to serialize and de-serialize the parameter.
	 * @return identifier
	 */
	public String getIdentifier();
}
