package cwcore;

import java.lang.reflect.Field;

import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfXMLTag;
import cwcore.complexParams.NamedParam;

/**
 * Number of bits assigned to the given parameter within GeneticController
 */
public class StateSize extends AbstractReflectionParams implements NamedParam {

	private static final long serialVersionUID = 1776929913108636457L;

	@ConfXMLTag("Name")
	public String name;

	@ConfXMLTag("Size")
	public int size;

	@Override
	public String getName() {
		return name + " bits";
	}

	@Override
	public Field getField() {
		try {
			return StateSize.class.getField("size");
		} catch (SecurityException ex) {
			// not going to happen
		} catch (NoSuchFieldException ex) {
			// not going to happen
		}
		// not going to happen
		return null;
	}
}
