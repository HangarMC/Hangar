package io.papermc.hangar.controller.extras.pagination.annotations;

import io.papermc.hangar.controller.extras.pagination.Filter;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicableFilters {

    /**
     * Set of applicable filters for this request
     */
    Class<? extends Filter<? extends Filter.FilterInstance, ?>>[] value();
}
