package org.cobweb.cobweb2.interconnect;

import org.cobweb.cobweb2.core.ComplexAgent;


public interface StateParameter {

	public String getName();

	public double getValue(ComplexAgent agent);
}
