package io.papermc.hangar.security.annotations.privileged;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.annotation.Secured;

/**
 * Require the user be logged in AND unlocked
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Secured("ROLE_USER")
public @interface Privileged {
}
