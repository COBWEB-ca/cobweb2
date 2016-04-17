package org.cobweb.cobweb2.ui.config;

import java.lang.reflect.AnnotatedElement;

/**
 * Allows a property of an object to be read and written.
 */
public interface PropertyAccessor {

	/**
	 * Name for this property
	 * @return Name for this property
	 */
	public String getName();

	public String getXmlName();

	/**
	 * Gets value of property for given object
	 * @param object object to get value from
	 * @return value of property for object
	 */
	public Object get(Object object);


	public float getAsFloat(Object object);

	/**
	 * Sets value of property for given object
	 * @param object object to set value on
	 * @param value value to set
	 */
	public void set(Object object, Object value);

	public void setAsFloat(Object object, float value);

	/**
	 * Class of the property
	 * @return class of the property
	 */
	public Class<?> getType();

	public AnnotatedElement getAnnotationSource();
}
