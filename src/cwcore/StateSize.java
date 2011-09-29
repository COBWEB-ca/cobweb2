package cwcore;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfXMLTag;

/**
 * Number of bits assigned to the given parameter
 */
public class StateSize extends AbstractReflectionParams {

	private static final long serialVersionUID = 1776929913108636457L;

	@ConfXMLTag("Name")
	public String name;

	@ConfXMLTag("Size")
	public int size;
}
