package io.papermc.hangar.security.annotations.currentuser;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires that the user referenced in the method be the current user
 * <b>OR</b> that the current user has the
 * {@link io.papermc.hangar.model.common.Permission#EditAllUserSettings} permission
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
    /**
     * interchangeable with {@link #userArgument()}
     * @return the SpEL string for the argument in a method
     */
    @AliasFor("userArgument")
    String value() default "";

    /**
     * interchangeable with {@link #value()}
     * @return the SpEL string for the argument in a method
     */
    @AliasFor("value")
    String userArgument() default "";
}
