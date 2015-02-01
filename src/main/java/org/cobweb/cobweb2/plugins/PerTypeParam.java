package org.cobweb.cobweb2.plugins;

import org.cobweb.io.ParameterSerializable;

/**
 * Parameter that depends on the number of agent types in the simulation
 */
public interface PerTypeParam<T extends ParameterSerializable> extends ResizableParam {

	public T[] getPerTypeParams();
}
