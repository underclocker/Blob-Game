package org.siggd.actor.meta;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for declaring a getter or setter to belong to a property.
 * @author mysterymath
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Prop {
	public String name();
	public boolean isActor() default false;
}
