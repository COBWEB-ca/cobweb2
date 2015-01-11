package org.cobweb.cobweb2.io;

import java.util.List;


public interface CobwebSelectionParam<T> extends CobwebParam {

	public List<T> getPossibleValues();
}
