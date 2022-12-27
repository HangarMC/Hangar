package io.papermc.hangar.controller.extras.pagination.annotations;

import io.papermc.hangar.controller.extras.pagination.SorterRegistry;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicableSorters {

    /**
     * Set of sorters applicable to this request
     */
    SorterRegistry[] value();
}
