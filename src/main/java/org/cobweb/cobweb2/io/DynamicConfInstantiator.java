package org.cobweb.cobweb2.io;


/**
 * Allows instantiation of a configuration parameter based on other parameters
 */
public interface DynamicConfInstantiator {
	/**
	 * Instantiate labelled parameter object
	 * @param parentConf configuration parent node
	 * @return new parameter object
	 */
	public CobwebParam instantiate(CobwebParam parentConf);
}
