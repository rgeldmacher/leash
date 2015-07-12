package com.rgeldmacher.leash.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author robertgeldmacher
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Retain {
}
