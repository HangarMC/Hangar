package io.papermc.hangar.security.annotations.permission;

import io.papermc.hangar.model.common.NamedPermission;
import io.papermc.hangar.model.common.PermissionType;
import io.papermc.hangar.security.annotations.permission.PermissionRequired.List;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.intellij.lang.annotations.Language;
import org.springframework.core.annotation.AliasFor;

/**
 * Mark a method or class to require permissions
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(List.class)
@Documented
public @interface PermissionRequired {

    /**
     * Permissions required
     */
    @AliasFor("perms")
    NamedPermission[] value() default {};

    /**
     * Type of permissions required
     */
    PermissionType type() default PermissionType.GLOBAL;

    /**
     * Permissions required
     */
    @AliasFor("value")
    NamedPermission[] perms() default {};

    /**
     * Method arguments to use when querying permissions
     * <br>
     * <b>Only used for {@link PermissionType#PROJECT} or {@link PermissionType#ORGANIZATION}</b>
     * <br>
     * The length of the SpEL array <b>must</b> match the type;
     */
    // @el(projectId: String)
    @Language("SpEL")
    String args() default "{}";

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        PermissionRequired[] value();
    }
}
