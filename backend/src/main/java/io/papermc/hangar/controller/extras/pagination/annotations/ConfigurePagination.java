package io.papermc.hangar.controller.extras.pagination.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.intellij.lang.annotations.Language;

/**
 * Configure default page length
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurePagination {

    long maxLimit() default -1;

    @Language("SpEL")
    String maxLimitString() default "";

    long defaultLimit() default -1;

    @Language("SpEL")
    String defaultLimitString() default "";
}
