package org.cobweb.io;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Name patterns to use for saving/loading Maps
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfMap {

	/**
	 * Name for map entries
	 */
	String entryName();

	/**
	 * Name for map keys
	 */
	String keyName();

	/**
	 * Declared class of map values.
	 * Required because of type erasure.
	 * Key class must be String to keep serialization simple.
	 */
	Class<?> valueClass();
}
