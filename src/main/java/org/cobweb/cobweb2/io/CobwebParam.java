package org.cobweb.cobweb2.io;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class contains the methods necessary to alter data from
 * the simulation configuration files.
 * 
 * @author ???
 */
public interface CobwebParam extends Serializable, Cloneable {
	public void loadConfig(Node root) throws IllegalArgumentException;

	public void saveConfig(Node root, Document document);
}
