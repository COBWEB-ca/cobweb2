package org.cobweb.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.cobweb.cobweb2.io.DynamicConfInstantiator;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfDynamicInstance {
	Class<? extends DynamicConfInstantiator> value();
}
