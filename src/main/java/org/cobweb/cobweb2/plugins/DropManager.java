package org.cobweb.cobweb2.plugins;

import org.cobweb.cobweb2.core.Drop;


public interface DropManager<T extends Drop> {

	public void remove(T drop);
}
