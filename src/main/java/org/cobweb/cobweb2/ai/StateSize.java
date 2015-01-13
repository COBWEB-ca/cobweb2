package org.cobweb.cobweb2.ai;

import java.lang.reflect.Field;

import org.cobweb.cobweb2.io.NamedParam;
import org.cobweb.io.ConfXMLTag;
import org.cobweb.io.ParameterSerializable;

/**
 * Number of bits assigned to the given parameter within GeneticController
 */
public class StateSize implements ParameterSerializable, NamedParam {

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
