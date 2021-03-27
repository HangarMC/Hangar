package io.papermc.hangar.controller.extras.pagination.annotations;

import io.papermc.hangar.controller.extras.pagination.SorterRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicableSorters {

    /**
     * use {@link SorterRegistry} static final strings
     */
    String[] value();
}
