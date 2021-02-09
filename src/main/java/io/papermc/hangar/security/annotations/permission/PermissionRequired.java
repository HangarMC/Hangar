package io.papermc.hangar.security.annotations.permission;

import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.modelold.NamedPermission;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionRequired {
    PermissionType type();
    NamedPermission[] perms();
    String args() default "{}";
}
