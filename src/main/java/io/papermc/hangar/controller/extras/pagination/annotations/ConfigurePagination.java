package io.papermc.hangar.controller.extras.pagination.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configure default page length/number
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurePagination {

    /**
     * -1 means fallback to default configured value
     */
    long maxLimit();
}
