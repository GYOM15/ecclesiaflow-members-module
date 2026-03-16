package com.ecclesiaflow.business.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the scopes required to invoke a method.
 *
 * <p>Intercepted by {@link ScopeValidationAspect}. By default (OR logic),
 * the user needs at least one of the listed scopes. Set {@code requireAll = true}
 * for AND logic.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireScopes {

    /** Scopes required to access the method. */
    String[] value();

    /** If true, ALL scopes are required (AND); if false, ANY scope suffices (OR). */
    boolean requireAll() default false;
}
