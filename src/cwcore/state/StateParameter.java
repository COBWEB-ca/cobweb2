package cwcore.state;

import cwcore.ComplexAgent;


public interface StateParameter {

	public String getName();

	public double getValue(ComplexAgent agent);
}
