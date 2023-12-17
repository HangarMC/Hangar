package io.papermc.hangar.exceptions.handlers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorRedirect {

    String RETURN_URL = "hangar:returnUrl";

    String returnUrl() default "";
}
