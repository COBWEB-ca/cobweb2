package org.cobweb.io;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

@Deprecated //FIXME: ParameterSerializable should not know about serializer
public interface ParameterCustomSerializable extends ParameterSerializable {
	public void loadConfig(Node node);
	public void saveConfig(Node node, Document document);
}
