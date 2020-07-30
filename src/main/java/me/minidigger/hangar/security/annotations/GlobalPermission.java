package me.minidigger.hangar.security.annotations;

import me.minidigger.hangar.model.NamedPermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalPermission {
    NamedPermission[] value();
}
