package org.cobweb.cobweb2.io;

import java.util.List;

import org.cobweb.io.ParameterSerializable;


public interface CobwebSelectionParam<T> extends ParameterSerializable {

	public List<T> getPossibleValues();
}
