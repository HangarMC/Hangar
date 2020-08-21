package io.papermc.hangar.security.annotations;

import io.papermc.hangar.model.NamedPermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ProjectPermission {
    NamedPermission[] value() default {};
}
