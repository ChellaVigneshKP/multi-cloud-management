package com.multicloud.commonlib.annotations.masksensitive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to mark fields or classes as containing sensitive data.
 * Fields with this annotation will be masked during logging or string building.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface MaskSensitive {
    /**
     * Whether to apply partial masking (e.g., show first and last few characters).
     *
     * @return true if partial masking should be used
     */
    boolean partial() default false;
}