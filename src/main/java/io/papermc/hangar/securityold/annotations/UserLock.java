package io.papermc.hangar.securityold.annotations;

import io.papermc.hangar.util.Routes;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Deprecated(forRemoval = true)
public @interface UserLock {
    @AliasFor("route")
    Routes value() default Routes.SHOW_HOME;
    @AliasFor("value")
    Routes route() default Routes.SHOW_HOME;

    String args() default "{}";
}
