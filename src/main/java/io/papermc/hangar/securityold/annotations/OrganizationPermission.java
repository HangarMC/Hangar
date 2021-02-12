package io.papermc.hangar.securityold.annotations;

import io.papermc.hangar.model.common.NamedPermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OrganizationPermission {
    NamedPermission[] value();
}
