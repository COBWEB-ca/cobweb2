package org.cobweb.cobweb2.io;

import java.util.Collection;

public interface CobwebSelectionParam<T> extends CobwebParam {

	public Collection<T> getPossibleValues();

	public void setValue(T value);
}
