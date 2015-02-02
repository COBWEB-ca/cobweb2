package org.cobweb.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Format string for displaying Array and Map items
 * Array use: String.format("%s %i", fieldConfDisplayName, arrayIndex)
 * Map use: String.format("%s thing", mapKey)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfDisplayFormat {
	String value();
}
