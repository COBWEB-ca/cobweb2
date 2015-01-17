package org.cobweb.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This field should not have its own configuration section, any child properties should be merged with parent
 * Has priority over {@link ConfXMLTag}
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfSquishParent {
}
