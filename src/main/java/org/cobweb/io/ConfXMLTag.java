package org.cobweb.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines @ConfXMLTag, which returns the value of a
 * specified data field.
 * 
 * @author ???
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfXMLTag {
	String value();
}
