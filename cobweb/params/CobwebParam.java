package cobweb.params;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface CobwebParam extends Serializable, Cloneable {
	public void loadConfig(Node root) throws IllegalArgumentException;

	public void saveConfig(Node root, Document document);
}
